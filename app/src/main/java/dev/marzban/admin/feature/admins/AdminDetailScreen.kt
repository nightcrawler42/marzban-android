package dev.marzban.admin.feature.admins

import dev.marzban.admin.core.ui.LocalSnackbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.KeyValueRow
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.core.util.formatBytes

@Composable
fun AdminDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: AdminDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is AdminAction.Toast -> snackbar.showSnackbar(action.message)
                is AdminAction.Deleted -> onDeleted()
            }
        }
    }

    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val username = (state as? UiState.Success)?.value?.admin?.username ?: ""

    TitleScaffold(
        title = username.ifBlank { "Admin" },
        onBack = onBack,
        actions = {
            IconButton(onClick = { if (username.isNotBlank()) onEdit(username) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Disable users") }, onClick = { menuOpen = false; viewModel.disableUsers() })
                DropdownMenuItem(text = { Text("Activate users") }, onClick = { menuOpen = false; viewModel.activateUsers() })
                DropdownMenuItem(text = { Text("Reset usage") }, onClick = { menuOpen = false; viewModel.resetUsage() })
                DropdownMenuItem(
                    text = { Text("Delete admin", color = MaterialTheme.colorScheme.error) },
                    onClick = { menuOpen = false; confirmDelete = true },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            KeyValueRow("Username", s.value.admin.username)
                            KeyValueRow("Sudo", if (s.value.admin.isSudo) "yes" else "no")
                            KeyValueRow("Telegram ID", s.value.admin.telegramId?.toString() ?: "—")
                            KeyValueRow("Discord webhook", s.value.admin.discordWebhook ?: "—")
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Usage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(formatBytes(s.value.usage ?: s.value.admin.usersUsage ?: 0))
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete admin?") },
            text = { Text("All users owned by this admin will remain. This action cannot be undone.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }},
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
