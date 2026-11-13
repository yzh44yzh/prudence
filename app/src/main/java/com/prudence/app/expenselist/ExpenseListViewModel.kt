package com.prudence.app.expenselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Category
import com.prudence.app.settings.SettingsRepository
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
    val currencySymbol: String = "",
)

class ExpenseListViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<ExpenseListState> = combine(
        expenseRepository.observeExpenses(),
        categoryRepository.observeCategories(),
        settingsRepository.displayCurrency,
    ) { expenses, categories, currency ->
        val month = YearMonth.now()
        ExpenseListState(
            rows = groupExpensesByDate(
                expenses = expenses,
                categoryNames = categories.associate { it.id to it.name },
                uncategorizedName = categories.firstOrNull { it.id == Category.UNCATEGORIZED_ID }?.name
                    ?: "Uncategorized",
                today = LocalDate.now(),
            ),
            monthLabel = month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                .replaceFirstChar { it.uppercase() },
            monthTotalMinor = monthTotalMinor(expenses, month),
            currencySymbol = currency,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseListState())
}
