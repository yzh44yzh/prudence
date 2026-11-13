package com.prudence.app.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Category
import com.prudence.app.domain.pathTo
import com.prudence.app.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ReportViewModel(
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    data class Row(
        val category: Category,
        val totalMinor: Long,
        /** Share of the interval total at top level, of the drilled category's total when drilled. */
        val shareOfLevel: Float,
        val hasChildren: Boolean,
    )

    data class State(
        val interval: Interval,
        val intervalLabel: String,
        val totalMinor: Long,
        val rows: List<Row>,
        /** Path from root to the drilled category; empty = top level. */
        val breadcrumb: List<Category>,
        val currencySymbol: String = "",
    )

    private val interval = MutableStateFlow(Interval.thisWeek(LocalDate.now()))
    private val drilledCategoryId = MutableStateFlow<Long?>(null)

    val state: StateFlow<State> = combine(
        interval,
        drilledCategoryId,
        expenseRepository.observeExpenses(),
        categoryRepository.observeCategories(),
    ) { interval, drilledId, expenses, categories ->
        // A drilled category that vanished from the tree resets the drill.
        val drilled = drilledId?.takeIf { id -> categories.any { it.id == id } }
        val inInterval = expenses.filter { it.date in interval }
        val total = inInterval.sumOf { it.amountMinor }

        // Denominator per SPEC.md §6: interval total at top level, drilled category's total when drilled.
        val drilledTotal = drilled?.let { subtreeTotalMinor(inInterval, categories, it) }
        val shareBase = drilledTotal ?: total

        val rows = breakdownRows(inInterval, categories, parentId = drilled).map { row ->
            Row(
                category = row.category,
                totalMinor = row.totalMinor,
                shareOfLevel = if (shareBase > 0) row.totalMinor.toFloat() / shareBase else 0f,
                // Only children with spending are worth drilling into (zero-spend rows are hidden).
                hasChildren = breakdownRows(inInterval, categories, row.category.id).isNotEmpty(),
            )
        }

        State(
            interval = interval,
            intervalLabel = intervalLabel(interval),
            totalMinor = total,
            rows = rows,
            breadcrumb = drilled?.let { categories.pathTo(it) }.orEmpty(),
        )
    }.combine(settingsRepository.displayCurrency) { state, currency ->
        state.copy(currencySymbol = currency)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        State(Interval.thisWeek(LocalDate.now()), "", 0L, emptyList(), emptyList()),
    )

    fun showWeek() {
        interval.value = Interval.thisWeek(LocalDate.now())
    }

    fun showMonth() {
        interval.value = Interval.thisMonth(LocalDate.now())
    }

    fun showCustom(start: LocalDate, end: LocalDate) {
        interval.value = Interval.custom(start, end)
    }

    fun shift(periods: Int) {
        interval.value = interval.value.shifted(periods)
    }

    /** Null jumps back to the top level. */
    fun drillTo(categoryId: Long?) {
        drilledCategoryId.value = categoryId
    }

    private fun intervalLabel(interval: Interval): String {
        val short = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        return when (interval.kind) {
            Interval.Kind.MONTH -> interval.start.month
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                .replaceFirstChar { it.uppercase() } + " " + interval.start.year
            else -> if (interval.start.month == interval.end.month) {
                "${interval.start.dayOfMonth}–${interval.end.format(short)} ${interval.end.year}"
            } else {
                "${interval.start.format(short)} – ${interval.end.format(short)} ${interval.end.year}"
            }
        }
    }
}
