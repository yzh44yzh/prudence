package com.prudence.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ExpenseList : NavKey

/** [expenseId] null = add mode; set = edit mode. */
@Serializable
data class AddExpense(val expenseId: Long? = null) : NavKey

@Serializable
data object Categories : NavKey
