package com.prudence.app.addexpense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prudence.app.domain.Category
import com.prudence.app.domain.Money
import com.prudence.app.domain.pathTo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: AddExpenseViewModel,
    onDone: () -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var dateIso by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val date = LocalDate.parse(dateIso)
    val amountMinor = remember(amount) { Money.parseMinor(amount) }
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    // First open of the form: preselect the last-used category (or Uncategorized).
    LaunchedEffect(Unit) {
        if (selectedCategoryId == null) selectedCategoryId = viewModel.defaultCategoryId()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add expense") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            FormRow(label = "Date") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                ) {
                    Text(date.format(dateFormatter), modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.DateRange, contentDescription = "Pick date")
                }
            }

            FormRow(label = "Category") {
                CategoryChipPicker(
                    categories = categories,
                    selectedId = selectedCategoryId,
                    onSelect = { selectedCategoryId = it },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.save(amountMinor ?: return@Button, date, note, selectedCategoryId, onSaved = onDone)
                },
                enabled = amountMinor != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        dateIso = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun FormRow(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

/**
 * Variant-B chip-path picker: the selected path renders as ✕-chips (tap trims that level and
 * everything below it); below them, chips offer the children of the selection (roots when
 * nothing is selected) — tap goes one level deeper and selects.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChipPicker(
    categories: List<Category>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    val path = remember(categories, selectedId) { selectedId?.let { categories.pathTo(it) }.orEmpty() }
    val nextLevel = remember(categories, path) {
        val childrenByParent = categories.groupBy { it.parentId }
        // The selection's children when it has any (go deeper); otherwise its siblings
        // (switch sideways — matches the Variant-B wireframe); roots when nothing is selected.
        val ownChildren = path.lastOrNull()?.let { childrenByParent[it.id] }.orEmpty()
        when {
            path.isEmpty() -> childrenByParent[null].orEmpty()
            ownChildren.isNotEmpty() -> ownChildren
            else -> childrenByParent[path.last().parentId].orEmpty()
        }.sortedBy { it.id }
    }

    if (path.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            path.forEach { category ->
                InputChip(
                    selected = true,
                    onClick = { onSelect(category.parentId) },
                    label = { Text(category.name) },
                    trailingIcon = {
                        Icon(Icons.Filled.Close, contentDescription = "Remove ${category.name} and below")
                    },
                )
            }
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        nextLevel.forEach { category ->
            FilterChip(
                selected = false,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
            )
        }
    }
}
