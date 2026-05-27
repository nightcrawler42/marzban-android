package dev.marzban.admin.feature.nodes

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.KeyValueRow
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.core.util.formatBytes

@Composable
fun NodeDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onLogs: (Int) -> Unit,
    onDeleted: () -> Unit,
    viewModel: NodeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is NodeAction.Toast -> Toast.makeText(context, action.message, Toast.LENGTH_SHORT).show()
                is NodeAction.Deleted -> onDeleted()
            }
        }
    }

    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val nodeId = viewModel.id

    TitleScaffold(
        title = (state as? UiState.Success)?.value?.node?.name ?: "Node",
        onBack = onBack,
        actions = {
            IconButton(onClick = { onLogs(nodeId) }) {
                Icon(Icons.Default.Article, contentDescription = "Logs")
            }
            IconButton(onClick = { onEdit(nodeId) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Reconnect") }, onClick = { menuOpen = false; viewModel.reconnect() }, leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) })
                DropdownMenuItem(text = { Text("Refresh") }, onClick = { menuOpen = false; viewModel.load() })
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
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
                            Text("Identity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            KeyValueRow("Name", s.value.node.name)
                            KeyValueRow("Address", s.value.node.address)
                            KeyValueRow("Port", s.value.node.port.toString())
                            KeyValueRow("API port", s.value.node.apiPort.toString())
                            KeyValueRow("Status", s.value.node.status.name.lowercase())
                            KeyValueRow("Xray version", s.value.node.xrayVersion ?: "—")
                            KeyValueRow("Usage coefficient", s.value.node.usageCoefficient.toString())
                            s.value.node.message?.let { KeyValueRow("Message", it) }
                        }
                    }
                    s.value.usage?.let { usage ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Bandwidth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                KeyValueRow("Uplink", formatBytes(usage.uplink))
                                KeyValueRow("Downlink", formatBytes(usage.downlink))
                            }
                        }
                    }
                    s.value.certificate?.let { cert ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("TLS certificate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text(cert, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = {
                                    val cm = ContextCompat.getSystemService(context, ClipboardManager::class.java)
                                    cm?.setPrimaryClip(ClipData.newPlainText("node certificate", cert))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                }) { Text("Copy certificate") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete node?") },
            text = { Text("Marzban will stop using this node.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }},
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
