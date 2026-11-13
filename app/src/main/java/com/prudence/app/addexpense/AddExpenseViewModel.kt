package com.prudence.app.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Category
import com.prudence.app.domain.Money
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

    /** The last-used category when it still exists in the tree, else Uncategorized. */
    suspend fun defaultCategoryId(): Long {
        val tree = categories.first { it.isNotEmpty() }
        val lastUsed = settingsRepository.lastUsedCategoryId.first()
        return lastUsed?.takeIf { id -> tree.any { it.id == id } } ?: Category.UNCATEGORIZED_ID
    }

    /** [amountMinor] must already be validated via [Money.parseMinor]; the Save button's enabled state guarantees it. */
    fun save(amountMinor: Long, date: LocalDate, note: String, categoryId: Long?, onSaved: () -> Unit) {
        val resolvedCategoryId = categoryId ?: Category.UNCATEGORIZED_ID
        viewModelScope.launch {
            expenseRepository.addExpense(
                amountMinor = amountMinor,
                date = date,
                note = note.ifBlank { null },
                categoryId = resolvedCategoryId,
            )
            settingsRepository.setLastUsedCategoryId(resolvedCategoryId)
            onSaved()
        }
    }
}
