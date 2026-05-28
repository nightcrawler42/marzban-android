package dev.marzban.admin.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onBack: (() -> Unit)? = null,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var intervalOpen by remember { mutableStateOf(false) }

    TitleScaffold(
        title = "Dashboard",
        onBack = onBack,
        actions = {
            IconButton(onClick = { intervalOpen = true }) {
                Icon(Icons.Default.Timer, contentDescription = "Auto refresh")
            }
            DropdownMenu(expanded = intervalOpen, onDismissRequest = { intervalOpen = false }) {
                Text(
                    "Auto refresh",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                RefreshInterval.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = { viewModel.setInterval(option); intervalOpen = false },
                        leadingIcon = {
                            if (state.interval == option) Icon(Icons.Default.Check, contentDescription = null)
                            else Spacer(Modifier.size(24.dp))
                        },
                    )
                }
            }
            IconButton(onClick = viewModel::refresh) {
                if (state.refreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val data = state.data) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(data.message, onRetry = viewModel::refresh)
                is UiState.Success -> DashboardContent(
                    system = data.value.system,
                    core = data.value.core,
                    usage = data.value.usage,
                    interval = state.interval,
                    lastUpdated = state.lastUpdated,
                    refreshing = state.refreshing,
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    system: SystemStatsDto?,
    core: CoreStatsDto?,
    usage: UsageAggregate?,
    interval: RefreshInterval,
    lastUpdated: Long?,
    refreshing: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 4.dp,
            bottom = 24.dp + dev.marzban.admin.core.ui.safeBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (system != null) {
            item { HeroHeader(system, core, interval, lastUpdated, refreshing) }
            item { GaugesRow(system) }
            item { UsersBreakdownCard(system) }
            if (usage != null) item { QuotaUsageCard(usage) }
            item { BandwidthCard(system) }
        }
        if (core != null) item { CoreCard(core) }
    }
}

@Composable
private fun HeroHeader(
    system: SystemStatsDto,
    core: CoreStatsDto?,
    interval: RefreshInterval,
    lastUpdated: Long?,
    refreshing: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(dev.marzban.admin.core.ui.theme.BrandGradients.hero)
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = refreshing, enter = fadeIn(), exit = fadeOut()) {
                    Row {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Text(
                    "Panel ${system.version}",
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.weight(1f))
                AutoRefreshChip(interval = interval)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${system.usersActive}",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Active users",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF9CFF9C), RoundedCornerShape(50)),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${system.onlineUsers} online now",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (core != null) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (core.started) Color(0xFF9CFF9C) else Color(0xFFFF9CA0),
                                shape = RoundedCornerShape(50),
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (core.started) "Xray ${core.version} running" else "Xray stopped",
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (lastUpdated != null) {
                    val time = remember(lastUpdated) {
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastUpdated))
                    }
                    Text(
                        time,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoRefreshChip(interval: RefreshInterval) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                if (interval == RefreshInterval.Off) "Auto-refresh off" else "Auto-refresh ${interval.label}",
                color = Color.White,
            )
        },
        leadingIcon = {
            if (interval != RefreshInterval.Off) Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White)
            else Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color.White)
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = Color.White.copy(alpha = 0.18f),
        ),
    )
}

@Composable
private fun GaugesRow(system: SystemStatsDto) {
    val memFraction = if (system.memTotal > 0) (system.memUsed.toFloat() / system.memTotal) else 0f
    val cpuFraction = (system.cpuUsage / 100.0).toFloat().coerceIn(0f, 1f)
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val gaugeSize = if (screenWidth < 360) 96.dp else 120.dp

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GaugeCard(
            modifier = Modifier.weight(1f),
            title = "CPU",
            value = "%.1f%%".format(system.cpuUsage),
            subtitle = "${system.cpuCores} cores",
            fraction = cpuFraction,
            icon = Icons.Default.Speed,
            gaugeSize = gaugeSize,
        )
        GaugeCard(
            modifier = Modifier.weight(1f),
            title = "Memory",
            value = "${formatBytes(system.memUsed)}",
            subtitle = "of ${formatBytes(system.memTotal)}",
            fraction = memFraction,
            icon = Icons.Default.Memory,
            gaugeSize = gaugeSize,
        )
    }
}

@Composable
private fun GaugeCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String,
    fraction: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gaugeSize: androidx.compose.ui.unit.Dp,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "gauge"
    )
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.size(gaugeSize), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    val inset = stroke / 2f
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF6F8BFF),
                                Color(0xFF21D4FD),
                                Color(0xFF8AE36F),
                                Color(0xFFF6C453),
                                Color(0xFFFF7E7E),
                            )
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * animated,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun UsersBreakdownCard(system: SystemStatsDto) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Users", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            StatRow("Total", system.totalUser, Color(0xFF6F8BFF))
            StatRow("Active", system.usersActive, dev.marzban.admin.core.ui.theme.StatusActive)
            StatRow("On hold", system.usersOnHold, dev.marzban.admin.core.ui.theme.StatusOnHold)
            StatRow("Disabled", system.usersDisabled, dev.marzban.admin.core.ui.theme.StatusDisabled)
            StatRow("Limited", system.usersLimited, dev.marzban.admin.core.ui.theme.StatusLimited)
            StatRow("Expired", system.usersExpired, dev.marzban.admin.core.ui.theme.StatusExpired)
        }
    }
}

@Composable
private fun StatRow(label: String, count: Int, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .background(accent, shape = RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BandwidthCard(system: SystemStatsDto) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Bandwidth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BandwidthStat("Incoming", formatBytes(system.incomingBandwidth), formatBytesPerSecond(system.incomingBandwidthSpeed), Color(0xFF21D4FD))
                BandwidthStat("Outgoing", formatBytes(system.outgoingBandwidth), formatBytesPerSecond(system.outgoingBandwidthSpeed), Color(0xFFF6C453))
            }
        }
    }
}

@Composable
private fun BandwidthStat(label: String, total: String, speed: String, accent: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(accent, RoundedCornerShape(50)))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
        Text(total, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(speed, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuotaUsageCard(usage: UsageAggregate) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Data quota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    "${usage.limitedCount} capped · ${usage.unlimitedCount} unlimited",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            val animated by animateFloatAsState(
                targetValue = usage.usedFraction,
                animationSpec = tween(durationMillis = 600),
                label = "quota"
            )
            androidx.compose.material3.LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuotaStat("Used", formatBytes(usage.totalUsed), Color(0xFF6F8BFF))
                QuotaStat(
                    "Remaining",
                    if (usage.totalQuota > 0L) formatBytes(usage.remaining) else "—",
                    Color(0xFF8AE36F),
                )
                QuotaStat(
                    "Total quota",
                    if (usage.totalQuota > 0L) formatBytes(usage.totalQuota) else "—",
                    Color(0xFFF6C453),
                )
            }
            if (usage.totalQuota <= 0L) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "No users have data limits yet.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QuotaStat(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(accent, RoundedCornerShape(50)))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CoreCard(core: CoreStatsDto) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (core.started) dev.marzban.admin.core.ui.theme.StatusActive else dev.marzban.admin.core.ui.theme.StatusExpired,
                            RoundedCornerShape(50)
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text("Xray core", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Version ${core.version}", style = MaterialTheme.typography.bodyMedium)
            Text(
                if (core.started) "Running" else "Stopped",
                style = MaterialTheme.typography.bodyMedium,
                color = if (core.started) dev.marzban.admin.core.ui.theme.StatusActive else dev.marzban.admin.core.ui.theme.StatusExpired,
            )
        }
    }
}
