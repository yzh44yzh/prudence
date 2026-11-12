package com.prudence.app.data

import com.prudence.app.domain.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExpenseRepository(private val dao: ExpenseDao) {

    fun observeExpenses(): Flow<List<Expense>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun addExpense(
        amountMinor: Long,
        date: LocalDate,
        note: String? = null,
        categoryId: Long = UNCATEGORIZED_ID,
    ) {
        dao.insert(
            ExpenseEntity(
                amountMinor = amountMinor,
                date = date,
                categoryId = categoryId,
                note = note,
            )
        )
    }

    private fun ExpenseEntity.toDomain() = Expense(
        id = id,
        amountMinor = amountMinor,
        date = date,
        categoryId = categoryId,
        note = note,
    )

    companion object {
        /** Placeholder for the permanent Uncategorized category until ticket #10 creates the tree. */
        const val UNCATEGORIZED_ID = 0L
    }
}
