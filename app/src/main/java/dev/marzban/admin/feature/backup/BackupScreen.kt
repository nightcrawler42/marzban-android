package dev.marzban.admin.feature.backup

import android.content.ClipData
import android.content.ClipboardManager
import dev.marzban.admin.core.ui.LocalSnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.marzban.admin.core.ui.TitleScaffold

private const val CLI_CMD = "marzban backup"
private const val CLI_DOCKER_CMD = "docker exec marzban marzban-cli backup"
private const val TELEGRAM_DOCS = "https://gozargah.github.io/marzban/en/docs/telegram-bot"

@Composable
fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    fun copy(value: String) {
        val cm = ContextCompat.getSystemService(context, ClipboardManager::class.java) ?: return
        cm.setPrimaryClip(ClipData.newPlainText("backup command", value))
        scope.launch { snackbar.showSnackbar("Copied") }
    }
    TitleScaffold(title = "Database backup", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { HeroBanner() }
            item { NoticeCard() }
            item {
                MethodCard(
                    title = "Run the CLI on your server",
                    body = "Triggers an immediate backup. The bundle is sent through your configured Telegram bot, so set TELEGRAM_API_TOKEN and TELEGRAM_ADMIN_ID first.",
                    icon = Icons.Default.Terminal,
                ) {
                    CommandRow(text = CLI_CMD, onCopy = { copy(CLI_CMD) })
                    Spacer(Modifier.height(6.dp))
                    Text("If you run Marzban with Docker:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    CommandRow(text = CLI_DOCKER_CMD, onCopy = { copy(CLI_DOCKER_CMD) })
                }
            }
            item {
                MethodCard(
                    title = "Schedule via Telegram bot",
                    body = "Marzban's Telegram bot can mail you a fresh backup on a fixed interval, plus an on-demand /backup command in the bot chat.",
                    icon = Icons.Default.CloudSync,
                ) {
                    Button(onClick = { uriHandler.openUri(TELEGRAM_DOCS) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open Telegram bot docs")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBanner() {
    val gradient = Brush.linearGradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Backup, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Backup", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Snapshot your panel's database & config", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun NoticeCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Marzban does not expose a backup REST endpoint.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "We can't trigger a backup from the phone over the API. Use one of the methods below from the server side.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun MethodCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CommandRow(text: String, onCopy: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0B0F19),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(
                text,
                color = Color(0xFFE8E8F0),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
            )
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
            }
        }
    }
}
