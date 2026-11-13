package com.prudence.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.CategoryRepository
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.export.toCsv
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val displayCurrency: StateFlow<String> = settingsRepository.displayCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setDisplayCurrency(symbol: String) {
        viewModelScope.launch { settingsRepository.setDisplayCurrency(symbol) }
    }

    /** Always a full export of all expenses (the backup mechanism, SPEC.md §7). */
    suspend fun buildCsv(): String = toCsv(
        expenses = expenseRepository.getExpenses(),
        categories = categoryRepository.getCategories(),
    )
}
