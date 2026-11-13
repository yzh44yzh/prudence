package com.prudence.app.export

import com.prudence.app.domain.Category
import com.prudence.app.domain.Expense
import com.prudence.app.domain.pathTo
import java.util.Locale

/**
 * Full-backup CSV per SPEC.md §7: header + `date,amount,category,note`; strict RFC 4180
 * (comma delimiter, quoted fields when they contain comma/quote/newline, doubled quotes,
 * CRLF line endings); chronological order (date, then id); dot-decimal major-unit amounts
 * regardless of the display-currency setting.
 */
fun toCsv(expenses: List<Expense>, categories: List<Category>): String {
    val sb = StringBuilder("date,amount,category,note\r\n")
    expenses.sortedWith(compareBy({ it.date }, { it.id })).forEach { expense ->
        sb.append(expense.date.toString()).append(',')
            .append(String.format(Locale.ROOT, "%d.%02d", expense.amountMinor / 100, expense.amountMinor % 100))
            .append(',')
            .append(escape(categories.pathTo(expense.categoryId).joinToString("/") { it.name }))
            .append(',')
            .append(escape(expense.note.orEmpty()))
            .append("\r\n")
    }
    return sb.toString()
}

private fun escape(field: String): String =
    if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
        "\"" + field.replace("\"", "\"\"") + "\""
    } else {
        field
    }
