package com.prudence.app

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.prudence.app.addexpense.AddExpenseViewModel
import com.prudence.app.data.AppDatabase
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.expenselist.ExpenseListViewModel

/** Manual DI: wires database -> DAO -> repository -> ViewModel factories. No framework (per spec). */
class AppContainer(context: Context) {

    private val database: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "prudence.db").build()

    private val expenseRepository = ExpenseRepository(database.expenseDao())

    fun expenseListViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { ExpenseListViewModel(expenseRepository) }
    }

    fun addExpenseViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { AddExpenseViewModel(expenseRepository) }
    }
}
