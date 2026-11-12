package com.prudence.app.expenselist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Expense
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExpenseListViewModel(repository: ExpenseRepository) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository.observeExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
