package com.prudence.app.data

import com.prudence.app.domain.Category
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
        categoryId: Long = Category.UNCATEGORIZED_ID,
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

    suspend fun getExpense(id: Long): Expense? = dao.getById(id)?.toDomain()

    suspend fun updateExpense(expense: Expense) {
        dao.update(expense.toEntity())
    }

    suspend fun deleteExpense(id: Long) {
        dao.deleteById(id)
    }

    private fun ExpenseEntity.toDomain() = Expense(
        id = id,
        amountMinor = amountMinor,
        date = date,
        categoryId = categoryId,
        note = note,
    )

    private fun Expense.toEntity() = ExpenseEntity(
        id = id,
        amountMinor = amountMinor,
        date = date,
        categoryId = categoryId,
        note = note,
    )
}
