package dev.marzban.admin.feature.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.KeyValueRow
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.core.util.formatBytes
import dev.marzban.admin.core.util.formatEpoch
import dev.marzban.admin.core.util.formatIso
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.feature.subscription.SubscriptionContent

@Composable
fun UserDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: UserDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is UserAction.Toast -> Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
                is UserAction.Deleted -> onDeleted()
            }
        }
    }

    var menuOpen by remember { mutableStateOf(false) }
    var subscriptionOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var ownerDialog by remember { mutableStateOf(false) }

    val username = (state as? UiState.Success)?.value?.username ?: ""

    TitleScaffold(
        title = username.ifBlank { "User" },
        onBack = onBack,
        actions = {
            IconButton(onClick = { subscriptionOpen = true }) {
                Icon(Icons.Default.QrCode, contentDescription = "Subscription")
            }
            IconButton(onClick = { viewModel.load() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
            IconButton(onClick = { if (username.isNotBlank()) onEdit(username) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Reset usage") }, onClick = { menuOpen = false; viewModel.resetUsage() })
                DropdownMenuItem(text = { Text("Revoke subscription") }, onClick = { menuOpen = false; viewModel.revokeSubscription() })
                DropdownMenuItem(text = { Text("Activate next plan") }, onClick = { menuOpen = false; viewModel.activateNextPlan() })
                DropdownMenuItem(text = { Text("Set owner") }, onClick = { menuOpen = false; ownerDialog = true })
                DropdownMenuItem(text = { Text("Delete user", color = MaterialTheme.colorScheme.error) }, onClick = { menuOpen = false; confirmDelete = true }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> UserDetailContent(s.value)
            }
        }
    }

    if (subscriptionOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val user = (state as? UiState.Success)?.value
        if (user != null) {
            ModalBottomSheet(
                onDismissRequest = { subscriptionOpen = false },
                sheetState = sheetState,
            ) {
                SubscriptionContent(user.username, user.subscriptionUrl)
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete user?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
    if (ownerDialog) {
        var newOwner by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { ownerDialog = false },
            title = { Text("Set owner") },
            text = {
                OutlinedTextField(
                    value = newOwner,
                    onValueChange = { newOwner = it },
                    label = { Text("Admin username") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newOwner.isNotBlank()) viewModel.setOwner(newOwner.trim())
                    ownerDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { ownerDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun UserDetailContent(user: UserResponse) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StatusCard(user) }
        item { UsageCard(user) }
        item { InboundsCard(user) }
        item { MetaCard(user) }
        if (user.note != null) item { NoteCard(user.note) }
    }
}

@Composable
private fun StatusCard(user: UserResponse) {
    SectionCard("Status") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UserStatusBadge(user.status)
            Spacer(Modifier.weight(1f))
            user.admin?.let { Text("Owner: ${it.username}", style = MaterialTheme.typography.labelSmall) }
        }
        if (user.expire != null) {
            Spacer(Modifier.height(8.dp))
            KeyValueRow("Expires", formatEpoch(user.expire))
        }
    }
}

@Composable
private fun UsageCard(user: UserResponse) {
    SectionCard("Usage") {
        val limit = user.dataLimit
        if (limit != null && limit > 0) {
            val ratio = (user.usedTraffic.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${formatBytes(user.usedTraffic)} of ${formatBytes(limit)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(formatBytes(user.usedTraffic), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text("Lifetime: ${formatBytes(user.lifetimeUsedTraffic)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Reset: ${user.dataLimitResetStrategy.name.lowercase()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InboundsCard(user: UserResponse) {
    SectionCard("Inbounds") {
        if (user.inbounds.isEmpty()) {
            Text("None assigned", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@SectionCard
        }
        user.inbounds.forEach { (protocol, tags) ->
            Spacer(Modifier.height(4.dp))
            Text(protocol, style = MaterialTheme.typography.labelLarge)
            Text(tags.joinToString(", "), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetaCard(user: UserResponse) {
    SectionCard("Details") {
        KeyValueRow("Created", formatIso(user.createdAt))
        KeyValueRow("Last UA", user.subLastUserAgent ?: "—")
        KeyValueRow("Sub updated", formatIso(user.subUpdatedAt))
        KeyValueRow("Online", formatIso(user.onlineAt))
        user.onHoldExpireDuration?.let { KeyValueRow("On-hold duration (s)", it.toString()) }
        user.autoDeleteInDays?.let { KeyValueRow("Auto-delete in (days)", it.toString()) }
    }
}

@Composable
private fun NoteCard(note: String) {
    SectionCard("Note") { Text(note) }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}
