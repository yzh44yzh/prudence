package com.prudence.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.prudence.app.addexpense.AddExpenseScreen
import com.prudence.app.expenselist.ExpenseListScreen
import com.prudence.app.navigation.AddExpense
import com.prudence.app.navigation.ExpenseList
import com.prudence.app.ui.theme.PrudenceTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as App).container

        setContent {
            PrudenceTheme {
                val backStack = rememberNavBackStack(ExpenseList)
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<ExpenseList> {
                            ExpenseListScreen(
                                viewModel = viewModel(factory = container.expenseListViewModelFactory()),
                                onAddClick = { backStack.add(AddExpense) },
                            )
                        }
                        entry<AddExpense> {
                            AddExpenseScreen(
                                viewModel = viewModel(factory = container.addExpenseViewModelFactory()),
                                onDone = { backStack.removeLastOrNull() },
                            )
                        }
                    },
                )
            }
        }
    }
}
