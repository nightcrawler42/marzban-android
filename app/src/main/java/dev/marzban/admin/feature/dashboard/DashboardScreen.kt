package dev.marzban.admin.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.core.util.formatBytes
import dev.marzban.admin.core.util.formatBytesPerSecond
import dev.marzban.admin.data.dto.CoreStatsDto
import dev.marzban.admin.data.dto.SystemStatsDto

@Composable
fun DashboardScreen(
    onBack: (() -> Unit)? = null,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TitleScaffold(
        title = "Dashboard",
        onBack = onBack,
        actions = {
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::refresh)
                is UiState.Success -> DashboardContent(system = s.value.system, core = s.value.core)
            }
        }
    }
}

@Composable
private fun DashboardContent(system: SystemStatsDto?, core: CoreStatsDto?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (system != null) {
            item { SystemCard(system) }
            item { UsersCard(system) }
            item { BandwidthCard(system) }
        }
        if (core != null) item { CoreCard(core) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SystemCard(system: SystemStatsDto) {
    SectionCard("Resources") {
        val memUsage = if (system.memTotal > 0) system.memUsed.toFloat() / system.memTotal else 0f
        StatRow("CPU", "%.1f%%".format(system.cpuUsage), system.cpuUsage.toFloat() / 100f)
        StatRow("Memory", "${formatBytes(system.memUsed)} / ${formatBytes(system.memTotal)}", memUsage)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Panel ${system.version}", style = MaterialTheme.typography.labelSmall)
            Text("${system.cpuCores} CPU cores", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun UsersCard(system: SystemStatsDto) {
    SectionCard("Users") {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("Total", system.totalUser.toString())
            Stat("Online", system.onlineUsers.toString())
            Stat("Active", system.usersActive.toString())
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("On hold", system.usersOnHold.toString())
            Stat("Disabled", system.usersDisabled.toString())
            Stat("Expired", system.usersExpired.toString())
            Stat("Limited", system.usersLimited.toString())
        }
    }
}

@Composable
private fun BandwidthCard(system: SystemStatsDto) {
    SectionCard("Bandwidth") {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("Incoming", formatBytes(system.incomingBandwidth))
            Stat("Outgoing", formatBytes(system.outgoingBandwidth))
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("In speed", formatBytesPerSecond(system.incomingBandwidthSpeed))
            Stat("Out speed", formatBytesPerSecond(system.outgoingBandwidthSpeed))
        }
    }
}

@Composable
private fun CoreCard(core: CoreStatsDto) {
    SectionCard("Xray core") {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat("Version", core.version)
            Stat("Status", if (core.started) "running" else "stopped")
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun StatRow(label: String, value: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp),
        )
    }
}
