package com.prudence.app.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudence.app.data.ExpenseRepository
import com.prudence.app.domain.Money
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    /** Parses [amountInput]; on success persists with today's date and invokes [onSaved]. */
    fun save(amountInput: String, onSaved: () -> Unit) {
        val amountMinor = Money.parseMinor(amountInput) ?: return
        viewModelScope.launch {
            repository.addExpense(amountMinor = amountMinor, date = LocalDate.now())
            onSaved()
        }
    }
}
