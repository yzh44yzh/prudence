package com.prudence.app.export

import com.prudence.app.domain.Category
import com.prudence.app.domain.Expense
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CsvExporterTest {

    private val categories = listOf(
        Category(1, "Uncategorized", null),
        Category(2, "Food", null),
        Category(7, "At Home", 2),
    )

    private fun expense(id: Long, amount: Long, day: Int, categoryId: Long, note: String? = null) =
        Expense(id, amount, LocalDate.of(2026, 11, day), categoryId, note)

    @Test
    fun `header then rows oldest first, full category path, dot-decimal amounts`() {
        val csv = toCsv(
            expenses = listOf(
                expense(2, 4210, 13, 7, "Weekly shop"),
                expense(1, 5, 10, 1), // 0.05, Uncategorized
            ),
            categories = categories,
        )

        assertEquals(
            "date,amount,category,note\r\n" +
                "2026-11-10,0.05,Uncategorized,\r\n" +
                "2026-11-13,42.10,Food/At Home,Weekly shop\r\n",
            csv,
        )
    }

    @Test
    fun `RFC 4180 quoting for notes with comma, quote, and newline`() {
        val csv = toCsv(
            expenses = listOf(expense(1, 100, 13, 2, "said \"hi\", then\nleft")),
            categories = categories,
        )

        assertEquals(
            "date,amount,category,note\r\n" +
                "2026-11-13,1.00,Food,\"said \"\"hi\"\", then\nleft\"\r\n",
            csv,
        )
    }

    @Test
    fun `empty notes stay empty and unquoted`() {
        val csv = toCsv(listOf(expense(1, 90000, 1, 2)), categories)
        assertEquals("date,amount,category,note\r\n2026-11-01,900.00,Food,\r\n", csv)
    }
}
