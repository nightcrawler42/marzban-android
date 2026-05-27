package dev.marzban.admin.feature.core

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.KeyValueRow
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState

@Composable
fun CoreStatusScreen(
    onBack: () -> Unit,
    onConfig: () -> Unit,
    onLogs: () -> Unit,
    viewModel: CoreStatusViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    var confirmRestart by remember { mutableStateOf(false) }
    TitleScaffold(
        title = "Core",
        onBack = onBack,
        actions = {
            IconButton(onClick = onConfig) { Icon(Icons.Default.Settings, contentDescription = "Config") }
            IconButton(onClick = onLogs) { Icon(Icons.Default.Article, contentDescription = "Logs") }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Xray core", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            KeyValueRow("Version", s.value.version)
                            KeyValueRow("Running", if (s.value.started) "yes" else "no")
                            KeyValueRow("Logs WS path", s.value.logsWebsocket)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { confirmRestart = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Restart core")
                    }
                }
            }
        }
    }
    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("Restart Xray core?") },
            text = { Text("Active connections will be dropped briefly.") },
            confirmButton = { TextButton(onClick = { confirmRestart = false; viewModel.restart() }) { Text("Restart") } },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancel") } },
        )
    }
}
