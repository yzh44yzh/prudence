package com.prudence.app.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prudence.app.domain.DisplayCurrencies
import com.prudence.app.export.shareCsv
import kotlinx.coroutines.launch

private val CURRENCY_LABELS = mapOf("" to "None", "$" to "$ — dollar", "€" to "€ — euro", "₽" to "₽ — ruble", "BYN" to "BYN — Belarusian ruble", "£" to "£ — pound")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onCategoriesClick: () -> Unit,
) {
    val displayCurrency by viewModel.displayCurrency.collectAsStateWithLifecycle()
    var showCurrencyDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text("Categories") },
                supportingContent = { Text("Manage the category tree") },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onCategoriesClick),
            )
            ListItem(
                headlineContent = { Text("Display currency") },
                supportingContent = {
                    Text(CURRENCY_LABELS[displayCurrency] ?: displayCurrency)
                },
                modifier = Modifier.clickable { showCurrencyDialog = true },
            )
            ListItem(
                headlineContent = { Text("Export CSV") },
                supportingContent = { Text("All expenses, for backup") },
                modifier = Modifier.clickable {
                    scope.launch { shareCsv(context, viewModel.buildCsv()) }
                },
            )
        }
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Display currency") },
            text = {
                Column {
                    DisplayCurrencies.ALL.forEach { symbol ->
                        val label = CURRENCY_LABELS[symbol] ?: symbol
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDisplayCurrency(symbol)
                                    showCurrencyDialog = false
                                },
                        ) {
                            RadioButton(selected = displayCurrency == symbol, onClick = null)
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") } },
        )
    }
}
