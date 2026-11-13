package com.prudence.app.report

import com.prudence.app.domain.Category
import com.prudence.app.domain.Expense
import com.prudence.app.domain.descendantIdsOf

/** One breakdown row: a category and the rolled-up total of its whole subtree. */
data class BreakdownRow(val category: Category, val totalMinor: Long)

/**
 * Rolled-up breakdown one tree level at a time (SPEC.md §6): rows for the children of
 * [parentId] (roots when null), each summing its own expenses plus all descendants'.
 * Zero-spend categories are hidden; rows sort by amount descending.
 */
fun breakdownRows(
    expenses: List<Expense>,
    categories: List<Category>,
    parentId: Long?,
): List<BreakdownRow> {
    val byParent = categories.groupBy { it.parentId }
    return byParent[parentId].orEmpty().map { category ->
        BreakdownRow(
            category = category,
            totalMinor = subtreeTotalMinor(expenses, categories, category.id),
        )
    }
        .filter { it.totalMinor > 0 }
        .sortedWith(compareByDescending<BreakdownRow> { it.totalMinor }.thenBy { it.category.id })
}

/** Sum of [expenses] on [categoryId] and its whole subtree. */
fun subtreeTotalMinor(expenses: List<Expense>, categories: List<Category>, categoryId: Long): Long {
    val subtreeIds = categories.descendantIdsOf(categoryId) + categoryId
    return expenses.filter { it.categoryId in subtreeIds }.sumOf { it.amountMinor }
}
