package com.prudence.app.expenselist

import com.prudence.app.domain.AppDateFormatter
import com.prudence.app.domain.Expense
import java.time.LocalDate
import java.time.YearMonth

/** Rows of the home list: date group headers interleaved with expense items, newest first. */
sealed interface ExpenseListRow {
    data class DateHeader(val label: String, val date: LocalDate) : ExpenseListRow
    data class Item(val expense: Expense, val categoryName: String) : ExpenseListRow
}

/**
 * [expenses] must arrive sorted date DESC, id DESC (the DAO order). [uncategorizedName] is the
 * display name of the permanent Uncategorized category, used for expenses whose categoryId no
 * longer resolves (defensive; the tree semantics prevent it).
 */
fun groupExpensesByDate(
    expenses: List<Expense>,
    categoryNames: Map<Long, String>,
    uncategorizedName: String,
    today: LocalDate,
): List<ExpenseListRow> {
    val rows = mutableListOf<ExpenseListRow>()
    var currentDate: LocalDate? = null
    for (expense in expenses) {
        if (expense.date != currentDate) {
            currentDate = expense.date
            rows += ExpenseListRow.DateHeader(dateLabel(currentDate, today), currentDate)
        }
        rows += ExpenseListRow.Item(
            expense = expense,
            categoryName = categoryNames[expense.categoryId] ?: uncategorizedName,
        )
    }
    return rows
}

private fun dateLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(AppDateFormatter)
}

/** The running total for the app bar: expenses inside [month] only. */
fun monthTotalMinor(expenses: List<Expense>, month: YearMonth): Long =
    expenses.filter { YearMonth.from(it.date) == month }.sumOf { it.amountMinor }
