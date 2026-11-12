package com.prudence.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMinor: Long,
    val date: LocalDate,
    /** 0 = Uncategorized placeholder until the category tree lands (ticket #10). */
    val categoryId: Long,
    val note: String?,
)
