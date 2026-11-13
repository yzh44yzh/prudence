package com.prudence.app.report

import com.prudence.app.domain.Category
import com.prudence.app.domain.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IntervalTest {

    private val friday = LocalDate.of(2026, 11, 13) // a Friday

    @Test
    fun `this week is the ISO Monday-Sunday week containing today`() {
        val interval = Interval.thisWeek(friday)
        assertEquals(LocalDate.of(2026, 11, 9), interval.start)  // Monday
        assertEquals(LocalDate.of(2026, 11, 15), interval.end)   // Sunday, includes future days
    }

    @Test
    fun `this month is the calendar month`() {
        val interval = Interval.thisMonth(friday)
        assertEquals(LocalDate.of(2026, 11, 1), interval.start)
        assertEquals(LocalDate.of(2026, 11, 30), interval.end)
    }

    @Test
    fun `shifting a week moves seven days per step`() {
        assertEquals(LocalDate.of(2026, 11, 2), Interval.thisWeek(friday).shifted(-1).start)
        assertEquals(LocalDate.of(2026, 11, 22), Interval.thisWeek(friday).shifted(1).end)
    }

    @Test
    fun `shifting a month moves a calendar month per step`() {
        val shifted = Interval.thisMonth(friday).shifted(-2)
        assertEquals(LocalDate.of(2026, 9, 1), shifted.start)
        assertEquals(LocalDate.of(2026, 9, 30), shifted.end)
    }

    @Test
    fun `month shift keeps the full calendar month even into longer months`() {
        val december = Interval.thisMonth(friday).shifted(1) // Nov (30 days) -> Dec (31 days)
        assertEquals(LocalDate.of(2026, 12, 1), december.start)
        assertEquals(LocalDate.of(2026, 12, 31), december.end)
        val february = Interval.thisMonth(LocalDate.of(2027, 1, 20)).shifted(1) // Jan -> Feb 2027
        assertEquals(LocalDate.of(2027, 2, 1), february.start)
        assertEquals(LocalDate.of(2027, 2, 28), february.end)
    }

    @Test
    fun `shifting a custom interval moves it by its own length`() {
        val custom = Interval.custom(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10)) // 10 days
        val shifted = custom.shifted(1)
        assertEquals(LocalDate.of(2026, 11, 11), shifted.start)
        assertEquals(LocalDate.of(2026, 11, 20), shifted.end)
        assertEquals(LocalDate.of(2026, 11, 1), shifted.shifted(-1).start)
    }

    @Test
    fun `custom interval coerces reversed endpoints`() {
        val custom = Interval.custom(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 1))
        assertEquals(LocalDate.of(2026, 11, 1), custom.start)
        assertEquals(LocalDate.of(2026, 11, 10), custom.end)
    }

    @Test
    fun `contains is inclusive of both endpoints`() {
        val interval = Interval.thisWeek(friday)
        assertTrue(LocalDate.of(2026, 11, 9) in interval)
        assertTrue(LocalDate.of(2026, 11, 15) in interval)
        assertTrue(LocalDate.of(2026, 11, 8) !in interval)
        assertTrue(LocalDate.of(2026, 11, 16) !in interval)
    }
}

class BreakdownTest {

    private val categories = listOf(
        Category(1, "Uncategorized", null),
        Category(2, "Food", null),
        Category(7, "Groceries", 2),
        Category(8, "Eating out", 2),
        Category(9, "Weekly", 7),
        Category(3, "Transport", null),
        Category(4, "Health", null), // no spending — must be hidden
    )

    private fun expense(amount: Long, categoryId: Long) =
        Expense(id = 0, amountMinor = amount, date = LocalDate.of(2026, 11, 10), categoryId = categoryId, note = null)

    @Test
    fun `top level rolls descendants into roots, sorted by amount, zero-spend hidden`() {
        val expenses = listOf(
            expense(1000, 7),  // Groceries
            expense(500, 9),   // Weekly (grandchild of Food)
            expense(300, 8),   // Eating out
            expense(146, 3),   // Transport
            expense(50, 1),    // Uncategorized
        )

        val rows = breakdownRows(expenses, categories, parentId = null)

        assertEquals(listOf("Food", "Transport", "Uncategorized"), rows.map { it.category.name })
        assertEquals(listOf(1800L, 146L, 50L), rows.map { it.totalMinor }) // Food = 1000+500+300
    }

    @Test
    fun `drilled level shows the children of that category with their own rollups`() {
        val expenses = listOf(
            expense(1000, 7),
            expense(500, 9),   // rolls into Groceries when drilled into Food
            expense(300, 8),
        )

        val rows = breakdownRows(expenses, categories, parentId = 2) // inside Food

        assertEquals(listOf("Groceries", "Eating out"), rows.map { it.category.name })
        assertEquals(listOf(1500L, 300L), rows.map { it.totalMinor })
    }

    @Test
    fun `a category's own expenses count toward its rollup`() {
        val rows = breakdownRows(listOf(expense(700, 2), expense(300, 7)), categories, parentId = null)
        assertEquals(1000L, rows.single().totalMinor) // Food's own 700 + Groceries' 300
    }

    @Test
    fun `empty interval gives no rows`() {
        assertTrue(breakdownRows(emptyList(), categories, parentId = null).isEmpty())
    }
}
