package com.prudence.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val date: LocalDate,
    /** References categories.id (1 = Uncategorized). No FK: delete semantics reassign in a transaction (see CategoryRepository). */
    val categoryId: Long,
    val note: String?,
)
