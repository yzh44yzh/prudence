package com.prudence.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ExpenseList : NavKey

@Serializable
data object AddExpense : NavKey

@Serializable
data object Categories : NavKey
