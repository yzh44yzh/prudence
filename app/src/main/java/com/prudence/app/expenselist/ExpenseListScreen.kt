package com.prudence.app.expenselist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
    onExpenseClick: (expenseId: Long) -> Unit,
    onCategoriesClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.monthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(Money.format(state.monthTotalMinor), style = MaterialTheme.typography.titleLarge)
                    }
                },
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
            items(
                state.rows,
                key = { row ->
                    when (row) {
                        is ExpenseListRow.DateHeader -> "date-${row.date}"
                        is ExpenseListRow.Item -> "expense-${row.expense.id}"
                    }
                },
            ) { row ->
                when (row) {
                    is ExpenseListRow.DateHeader -> Text(
                        row.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    is ExpenseListRow.Item -> Column {
                        ListItem(
                            headlineContent = { Text(row.categoryName) },
                            supportingContent = row.expense.note?.let { note -> { Text(note) } },
                            trailingContent = { Text(Money.format(row.expense.amountMinor)) },
                            modifier = Modifier.clickable { onExpenseClick(row.expense.id) },
                        )
                        HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
