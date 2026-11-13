package com.prudence.app.expenselist

import com.prudence.app.domain.Category
import com.prudence.app.domain.Expense
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ExpenseListGroupingTest {

    private val today = LocalDate.of(2026, 11, 13) // Friday
    private val categories = mapOf(
        Category.UNCATEGORIZED_ID to "Uncategorized",
        2L to "Food",
        7L to "At Home",
    )

    private fun expense(id: Long, amount: Long, date: LocalDate, categoryId: Long = 2, note: String? = null) =
        Expense(id, amount, date, categoryId, note)

    @Test
    fun `groups expenses under date headers, newest date first`() {
        // DAO order: date DESC, id DESC
        val expenses = listOf(
            expense(3, 100, today),
            expense(2, 200, today.minusDays(1)),
            expense(1, 300, today.minusDays(1)),
        )

        val rows = groupExpensesByDate(expenses, categories, "Uncategorized", today)

        assertEquals(
            listOf(
                ExpenseListRow.DateHeader("Today", today),
                ExpenseListRow.Item(expense(3, 100, today), "Food"),
                ExpenseListRow.DateHeader("Yesterday", today.minusDays(1)),
                ExpenseListRow.Item(expense(2, 200, today.minusDays(1)), "Food"),
                ExpenseListRow.Item(expense(1, 300, today.minusDays(1)), "Food"),
            ),
            rows,
        )
    }

    @Test
    fun `older dates get a formatted label`() {
        val day = LocalDate.of(2026, 10, 5)
        val rows = groupExpensesByDate(listOf(expense(1, 100, day)), categories, "Uncategorized", today)
        assertEquals("5 Oct 2026", (rows[0] as ExpenseListRow.DateHeader).label)
    }

    @Test
    fun `expense with an unknown category shows as Uncategorized rather than crashing`() {
        val rows = groupExpensesByDate(listOf(expense(1, 100, today, categoryId = 999)), categories, "Uncategorized", today)
        assertEquals("Uncategorized", (rows[1] as ExpenseListRow.Item).categoryName)
    }

    @Test
    fun `month total sums only expenses inside the given month`() {
        val expenses = listOf(
            expense(1, 100, LocalDate.of(2026, 11, 1)),
            expense(2, 250, LocalDate.of(2026, 11, 13)),
            expense(3, 999, LocalDate.of(2026, 10, 31)), // October — excluded
        )
        assertEquals(350L, monthTotalMinor(expenses, YearMonth.of(2026, 11)))
        assertEquals(0L, monthTotalMinor(emptyList(), YearMonth.of(2026, 11)))
    }
}
