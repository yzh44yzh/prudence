package com.prudence.app.expenselist

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prudence.app.domain.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseListViewModel,
    onAddClick: () -> Unit,
    onCategoriesClick: () -> Unit,
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prudence") },
                actions = {
                    IconButton(onClick = onCategoriesClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Categories")
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
            ) {
                Text("+ Add expense")
            }
        },
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(expenses, key = { it.id }) { expense ->
                ListItem(
                    headlineContent = { Text(Money.format(expense.amountMinor)) },
                    supportingContent = { Text(expense.date.toString()) },
                )
            }
        }
    }
}
