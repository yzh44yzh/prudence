package com.prudence.app.categories

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prudence.app.domain.Category
import com.prudence.app.domain.descendantIdsOf

/** A category flattened for display: pre-order DFS with its depth in the tree. */
private data class CategoryRow(val category: Category, val depth: Int)

private fun buildRows(all: List<Category>): List<CategoryRow> {
    val childrenByParent = all.groupBy { it.parentId }
    fun walk(parentId: Long?, depth: Int): List<CategoryRow> =
        childrenByParent[parentId].orEmpty()
            .sortedBy { if (it.id == Category.UNCATEGORIZED_ID) Long.MAX_VALUE else it.id }
            .flatMap { listOf(CategoryRow(it, depth)) + walk(it.id, depth + 1) }
    return walk(null, 0)
}

private sealed interface CategoriesDialog {
    data object Add : CategoriesDialog
    data class Rename(val category: Category) : CategoriesDialog
    data class Move(val category: Category) : CategoriesDialog
    data class Delete(val category: Category) : CategoriesDialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBack: () -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<CategoriesDialog?>(null) }
    val rows = remember(categories) { buildRows(categories) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { dialog = CategoriesDialog.Add }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add category")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(rows, key = { it.category.id }) { row ->
                CategoryRow(
                    row = row,
                    onRename = { dialog = CategoriesDialog.Rename(row.category) },
                    onMove = { dialog = CategoriesDialog.Move(row.category) },
                    onDelete = { dialog = CategoriesDialog.Delete(row.category) },
                )
            }
        }
    }

    when (val active = dialog) {
        is CategoriesDialog.Add -> CategoryFormDialog(
            title = "New category",
            confirmLabel = "Add",
            initialName = "",
            parentOptions = parentOptions(rows),
            onConfirm = { name, parentId -> viewModel.addCategory(name, parentId) },
            onDismiss = { dialog = null },
        )
        is CategoriesDialog.Rename -> CategoryFormDialog(
            title = "Rename ${active.category.name}",
            confirmLabel = "Rename",
            initialName = active.category.name,
            parentOptions = null, // rename only; moves happen via Move
            onConfirm = { name, _ -> viewModel.renameCategory(active.category.id, name) },
            onDismiss = { dialog = null },
        )
        is CategoriesDialog.Move -> {
            val excluded = categories.descendantIdsOf(active.category.id) + active.category.id
            CategoryParentDialog(
                title = "Move ${active.category.name} under…",
                parentOptions = parentOptions(rows, exclude = excluded),
                onConfirm = { parentId -> viewModel.moveCategory(active.category.id, parentId) },
                onDismiss = { dialog = null },
            )
        }
        is CategoriesDialog.Delete -> {
            val sinkName = active.category.parentId?.let { pid ->
                categories.firstOrNull { it.id == pid }?.name
            } ?: "Uncategorized"
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text("Delete ${active.category.name}?") },
                text = { Text("Its expenses and subcategories move to $sinkName. Nothing is lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCategory(active.category.id)
                        dialog = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = null }) { Text("Cancel") }
                },
            )
        }
        null -> Unit
    }
}

/** Parent options as (id -> indented display name); null id = "make it a root". */
private fun parentOptions(
    rows: List<CategoryRow>,
    exclude: Set<Long> = emptySet(),
): List<Pair<Long?, String>> =
    listOf(null to "(root)") +
        rows
            .filter { it.category.id !in exclude }
            .map { row -> row.category.id to ("    ".repeat(row.depth) + row.category.name) }

@Composable
private fun CategoryRow(
    row: CategoryRow,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
) {
    val isUncategorized = row.category.id == Category.UNCATEGORIZED_ID
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (16 + row.depth * 20).dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
    ) {
        Text(
            row.category.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isUncategorized) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
        )
        if (isUncategorized) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = "Permanent category",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            IconButton(onClick = onRename) {
                Icon(Icons.Filled.Edit, contentDescription = "Rename")
            }
            var menuOpen by remember { mutableStateOf(false) }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More actions")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Move…") },
                    onClick = {
                        menuOpen = false
                        onMove()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

/** Name entry plus an optional parent picker (null parentOptions = no picker, e.g. rename). */
@Composable
private fun CategoryFormDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    parentOptions: List<Pair<Long?, String>>?,
    onConfirm: (name: String, parentId: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var parentId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (parentOptions != null) {
                ParentPicker(
                    options = parentOptions,
                    selected = parentId,
                    onSelect = { parentId = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, parentId); onDismiss() },
                enabled = name.isNotBlank(),
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CategoryParentDialog(
    title: String,
    parentOptions: List<Pair<Long?, String>>,
    onConfirm: (parentId: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var parentId by remember { mutableStateOf<Long?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            ParentPicker(options = parentOptions, selected = parentId, onSelect = { parentId = it })
        },
        confirmButton = { TextButton(onClick = { onConfirm(parentId); onDismiss() }) { Text("Move") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ParentPicker(
    options: List<Pair<Long?, String>>,
    selected: Long?,
    onSelect: (Long?) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: "(root)"
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Text("Parent:", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = { open = true }) { Text(selectedLabel.trim()) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (id, label) ->
                DropdownMenuItem(
                    text = { Text(label.ifBlank { "(root)" }) },
                    onClick = {
                        open = false
                        onSelect(id)
                    },
                )
            }
        }
    }
}
