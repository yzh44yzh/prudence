package com.prudence.app.expenselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Whole-screen state, derived from one combine: rows and month summary can never disagree. */
data class ExpenseListState(
    val rows: List<ExpenseListRow> = emptyList(),
    val monthLabel: String = "",
    val monthTotalMinor: Long = 0L,
)

class ExpenseListViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    val state: StateFlow<ExpenseListState> = combine(
        expenseRepository.observeExpenses(),
        categoryRepository.observeCategories(),
    ) { expenses, categories ->
        val month = YearMonth.now()
        ExpenseListState(
            rows = groupExpensesByDate(
                expenses = expenses,
                categoryNames = categories.associate { it.id to it.name },
                uncategorizedName = categories.firstOrNull { it.id == Category.UNCATEGORIZED_ID }?.name
                    ?: "Uncategorized",
                today = LocalDate.now(),
            ),
            // FULL_STANDALONE: nominative case for a standalone label ("Ноябрь", not "Ноября")
            monthLabel = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                .replaceFirstChar { it.uppercase() },
            monthTotalMinor = monthTotalMinor(expenses, month),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseListState())
}
