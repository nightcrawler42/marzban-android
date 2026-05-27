package dev.marzban.admin.feature.hosts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.LocalSnackbarHostState
import dev.marzban.admin.core.ui.StatusPill
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.core.ui.hostPill
import dev.marzban.admin.core.ui.safeBottomPadding
import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.dto.HostSecurity
import kotlinx.coroutines.launch

@Composable
fun HostsScreen(
    onBack: () -> Unit,
    viewModel: HostsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { msg -> scope.launch { snackbar.showSnackbar(msg) } }
    }

    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    TitleScaffold(
        title = "Hosts",
        onBack = onBack,
        actions = {
            TextButton(onClick = viewModel::save) { Text("Save") }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> {
                    if (s.value.isEmpty()) {
                        EmptyState("No inbounds with hosts", "Configure inbounds on the panel first.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ).let { pv ->
                                PaddingValues(
                                    start = 16.dp, end = 16.dp,
                                    top = 12.dp,
                                    bottom = 12.dp + safeBottomPadding(),
                                )
                            },
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            s.value.forEach { (tag, hosts) ->
                                item(key = "hdr-$tag") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            tag,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(onClick = { viewModel.addHost(tag) }) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Add host")
                                        }
                                    }
                                }
                                hosts.forEachIndexed { index, host ->
                                    val cardKey = "$tag:$index"
                                    item(key = cardKey) {
                                        ExpandableHostCard(
                                            host = host,
                                            expanded = expanded[cardKey] ?: false,
                                            onExpandToggle = {
                                                expanded[cardKey] = !(expanded[cardKey] ?: false)
                                            },
                                            onChange = { viewModel.updateHost(tag, index, it) },
                                            onRemove = { viewModel.removeHost(tag, index) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableHostCard(
    host: HostDto,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onChange: (HostDto) -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Column {
            // Header row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            host.remark.ifBlank { "(no remark)" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusPill(tone = hostPill(host))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${host.address}${host.port?.let { ":$it" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
            // Expanded form
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = host.remark,
                        onValueChange = { onChange(host.copy(remark = it)) },
                        label = { Text("Remark") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = host.address,
                        onValueChange = { onChange(host.copy(address = it)) },
                        label = { Text("Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = host.port?.toString().orEmpty(),
                            onValueChange = { onChange(host.copy(port = it.toIntOrNull())) },
                            label = { Text("Port") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = host.sni.orEmpty(),
                            onValueChange = { onChange(host.copy(sni = it.ifBlank { null })) },
                            label = { Text("SNI") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = host.host.orEmpty(),
                            onValueChange = { onChange(host.copy(host = it.ifBlank { null })) },
                            label = { Text("Host header") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = host.path.orEmpty(),
                            onValueChange = { onChange(host.copy(path = it.ifBlank { null })) },
                            label = { Text("Path") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Text(
                        "TLS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            HostSecurity.InboundDefault to "default",
                            HostSecurity.None to "none",
                            HostSecurity.Tls to "tls",
                        )
                        options.forEachIndexed { i, (sec, label) ->
                            SegmentedButton(
                                selected = host.security == sec,
                                onClick = { onChange(host.copy(security = sec)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = i,
                                    count = options.size,
                                ),
                            ) { Text(label) }
                        }
                    }

                    ToggleRow(
                        label = "Disabled",
                        checked = host.isDisabled == true,
                        onChange = { onChange(host.copy(isDisabled = it)) },
                    )
                    ToggleRow(
                        label = "Mux enabled",
                        checked = host.muxEnable == true,
                        onChange = { onChange(host.copy(muxEnable = it)) },
                    )
                    ToggleRow(
                        label = "Allow insecure TLS",
                        checked = host.allowInsecure == true,
                        onChange = { onChange(host.copy(allowInsecure = it)) },
                    )
                    ToggleRow(
                        label = "Use SNI as host",
                        checked = host.useSniAsHost == true,
                        onChange = { onChange(host.copy(useSniAsHost = it)) },
                    )
                    ToggleRow(
                        label = "Random user-agent",
                        checked = host.randomUserAgent == true,
                        onChange = { onChange(host.copy(randomUserAgent = it)) },
                    )

                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = onRemove,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Remove host", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
