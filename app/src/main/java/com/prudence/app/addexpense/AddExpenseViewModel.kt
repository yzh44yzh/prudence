package com.prudence.app.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Category
import com.prudence.app.domain.Expense
import com.prudence.app.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val displayCurrency: StateFlow<String> = settingsRepository.displayCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /** The last-used category when it still exists in the tree, else Uncategorized. */
    suspend fun defaultCategoryId(): Long {
        val tree = categories.first { it.isNotEmpty() }
        val lastUsed = settingsRepository.lastUsedCategoryId.first()
        return lastUsed?.takeIf { id -> tree.any { it.id == id } } ?: Category.UNCATEGORIZED_ID
    }

    suspend fun expenseForEdit(id: Long): Expense? = expenseRepository.getExpense(id)

    /** [amountMinor] must already be validated via [Money.parseMinor]; the Save button's enabled state guarantees it. */
    fun save(expenseId: Long?, amountMinor: Long, date: LocalDate, note: String, categoryId: Long?, onSaved: () -> Unit) {
        val resolvedCategoryId = categoryId ?: Category.UNCATEGORIZED_ID
        viewModelScope.launch {
            if (expenseId == null) {
                expenseRepository.addExpense(
                    amountMinor = amountMinor,
                    date = date,
                    note = note.ifBlank { null },
                    categoryId = resolvedCategoryId,
                )
            } else {
                expenseRepository.updateExpense(
                    Expense(
                        id = expenseId,
                        amountMinor = amountMinor,
                        date = date,
                        categoryId = resolvedCategoryId,
                        note = note.ifBlank { null },
                    )
                )
            }
            // Last-used category is an add-form affordance (SPEC.md §5.2); edits don't touch it.
            if (expenseId == null) settingsRepository.setLastUsedCategoryId(resolvedCategoryId)
            onSaved()
        }
    }

    fun delete(expenseId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expenseId)
            onDeleted()
        }
    }
}
