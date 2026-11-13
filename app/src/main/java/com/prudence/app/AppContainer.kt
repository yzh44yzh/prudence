package com.prudence.app

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.prudence.app.addexpense.AddExpenseViewModel
import com.prudence.app.categories.CategoriesViewModel
import com.prudence.app.data.AppDatabase
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.CategorySeeds
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.expenselist.ExpenseListViewModel
import com.prudence.app.settings.SettingsRepository
import com.prudence.app.settings.settingsDataStore

/** Manual DI: wires database -> DAOs -> repositories -> ViewModel factories. No framework (per spec). */
class AppContainer(context: Context) {

    private val database: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "prudence.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addCallback(CategorySeeds.callback)
            .build()

    private val expenseRepository = ExpenseRepository(database.expenseDao())
    private val categoryRepository = CategoryRepository(database, database.categoryDao())
    private val settingsRepository = SettingsRepository(context.settingsDataStore)

    fun expenseListViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { ExpenseListViewModel(expenseRepository, categoryRepository) }
    }

    fun addExpenseViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { AddExpenseViewModel(expenseRepository, categoryRepository, settingsRepository) }
    }

    fun categoriesViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { CategoriesViewModel(categoryRepository) }
    }
}
