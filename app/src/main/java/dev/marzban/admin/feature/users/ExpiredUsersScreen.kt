package dev.marzban.admin.feature.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.EmptyState
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.HoldToConfirmButton
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.LocalSnackbarHostState
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.TypedConfirmDialog
import dev.marzban.admin.core.ui.UiState
import kotlinx.coroutines.launch

@Composable
fun ExpiredUsersScreen(
    onBack: () -> Unit,
    viewModel: ExpiredUsersViewModel = hiltViewModel(),
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> scope.launch { snackbar.showSnackbar(msg) } }
    }

    var deleteAllDialog by remember { mutableStateOf(false) }
    var resetAllSheet by remember { mutableStateOf(false) }
    var deleteSelectedDialog by remember { mutableStateOf(false) }

    val users = (ui.users as? UiState.Success)?.value.orEmpty()
    val allSelected = users.isNotEmpty() && users.all { ui.selected.contains(it) }

    TitleScaffold(
        title = if (ui.selected.isEmpty()) "Expired users"
                else "${ui.selected.size} of ${users.size} selected",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = { if (allSelected) viewModel.clearSelection() else viewModel.selectAll() },
                enabled = users.isNotEmpty(),
            ) {
                Icon(
                    Icons.Default.Checklist,
                    contentDescription = if (allSelected) "Clear selection" else "Select all",
                )
            }
            TextButton(onClick = { resetAllSheet = true }) { Text("Reset all") }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = ui.users) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> {
                    if (s.value.isEmpty()) {
                        EmptyState(
                            title = "No expired users",
                            subtitle = "All accounts are within their plans.",
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp,
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(s.value, key = { it }) { username ->
                                    UserCheckRow(
                                        username = username,
                                        selected = ui.selected.contains(username),
                                        onToggle = { viewModel.toggleSelection(username) },
                                    )
                                }
                            }
                            ActionBar(
                                selectedCount = ui.selected.size,
                                totalCount = s.value.size,
                                deleting = ui.deleting,
                                onDeleteSelected = { deleteSelectedDialog = true },
                                onDeleteAll = { deleteAllDialog = true },
                            )
                        }
                    }
                }
            }
        }
    }

    if (deleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { deleteSelectedDialog = false },
            title = { Text("Delete ${ui.selected.size} selected?") },
            text = {
                Column {
                    Text("These users will be permanently removed:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    val preview = ui.selected.take(8).joinToString("\n") { "• $it" }
                    Text(
                        preview + if (ui.selected.size > 8) "\n…and ${ui.selected.size - 8} more" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { deleteSelectedDialog = false; viewModel.deleteSelected() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { deleteSelectedDialog = false }) { Text("Cancel") } },
        )
    }

    if (deleteAllDialog) {
        TypedConfirmDialog(
            title = "Delete ALL expired users",
            body = "${users.size} accounts will be permanently removed. First: " +
                users.take(5).joinToString(", ") + (if (users.size > 5) ", …" else ""),
            expected = users.size.toString(),
            confirmLabel = "Delete ${users.size}",
            onConfirm = { viewModel.deleteAllExpired() },
            onDismiss = { deleteAllDialog = false },
        )
    }

    if (resetAllSheet) {
        AlertDialog(
            onDismissRequest = { resetAllSheet = false },
            title = { Text("Reset EVERY user's usage?") },
            text = {
                Column {
                    Text(
                        "This zeros lifetime traffic counters for every user on the panel — not just expired ones.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    HoldToConfirmButton(
                        label = "Hold to reset all",
                        onConfirm = { resetAllSheet = false; viewModel.resetAll() },
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { resetAllSheet = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun UserCheckRow(username: String, selected: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Text(username, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ActionBar(
    selectedCount: Int,
    totalCount: Int,
    deleting: Boolean,
    onDeleteSelected: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (deleting) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Working…")
                Spacer(Modifier.weight(1f))
            } else {
                OutlinedButton(
                    onClick = onDeleteAll,
                    enabled = totalCount > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete all ($totalCount)")
                }
                FilledTonalButton(
                    onClick = onDeleteSelected,
                    enabled = selectedCount > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete selected ($selectedCount)")
                }
            }
        }
    }
}
