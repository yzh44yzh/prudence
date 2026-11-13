package com.prudence.app.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prudence.app.domain.Money
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pickingStart by rememberSaveable { mutableStateOf(false) }
    var pickingEndAfterStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.padding(horizontal = 16.dp)) {
                SegmentedButton(
                    selected = state.interval.kind == Interval.Kind.WEEK,
                    onClick = { viewModel.showWeek() },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                ) { Text("Week") }
                SegmentedButton(
                    selected = state.interval.kind == Interval.Kind.MONTH,
                    onClick = { viewModel.showMonth() },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                ) { Text("Month") }
                SegmentedButton(
                    selected = state.interval.kind == Interval.Kind.CUSTOM,
                    onClick = { pickingStart = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                ) { Text("Custom") }
            }

            // Interval label + shift arrows (shift by the interval's own length, SPEC.md §6)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                IconButton(onClick = { viewModel.shift(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous interval")
                }
                Text(
                    state.intervalLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = { viewModel.shift(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next interval")
                }
            }

            Text(
                Money.format(state.totalMinor),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.breadcrumb.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        "All",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.drillTo(null) },
                    )
                    state.breadcrumb.forEach { category ->
                        Text(" / ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            category.name,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.drillTo(category.id) },
                        )
                    }
                }
            }

            if (state.rows.isEmpty() && state.breadcrumb.isEmpty()) {
                Text(
                    "No expenses in this interval",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                )
            } else {
                LazyColumn {
                    items(state.rows, key = { it.category.id }) { row ->
                        BreakdownRow(
                            row = row,
                            onClick = if (row.hasChildren) {
                                { viewModel.drillTo(row.category.id) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    // Custom interval: two date pickers in sequence, end >= start coerced by Interval.custom
    if (pickingStart) {
        IntervalDateDialog(
            title = "Start date",
            initial = state.interval.start,
            onConfirm = { start ->
                pickingStart = false
                pickingEndAfterStartMillis = start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            },
            onDismiss = { pickingStart = false },
        )
    }
    pickingEndAfterStartMillis?.let { startMillis ->
        val start = Instant.ofEpochMilli(startMillis).atZone(ZoneOffset.UTC).toLocalDate()
        IntervalDateDialog(
            title = "End date",
            initial = start,
            onConfirm = { end ->
                viewModel.showCustom(start, end)
                pickingEndAfterStartMillis = null
            },
            onDismiss = { pickingEndAfterStartMillis = null },
        )
    }
}

@Composable
private fun BreakdownRow(row: ReportViewModel.Row, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(row.category.name, modifier = Modifier.weight(1f))
            Text(Money.format(row.totalMinor), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "${(row.shareOfLevel * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Inline amount bar (plain Compose drawing — no chart library, SPEC.md §6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(row.shareOfLevel.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntervalDateDialog(
    title: String,
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                }
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
            )
            DatePicker(state = pickerState)
        }
    }
}
