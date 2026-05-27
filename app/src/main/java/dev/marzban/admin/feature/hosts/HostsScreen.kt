package dev.marzban.admin.feature.hosts

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.EmptyPlaceholder
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.dto.HostSecurity

@Composable
fun HostsScreen(
    onBack: () -> Unit,
    viewModel: HostsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
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
                    if (s.value.isEmpty()) EmptyPlaceholder("No inbounds with hosts.")
                    else LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        s.value.forEach { (tag, hosts) ->
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tag, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.weight(1f))
                                    TextButton(onClick = { viewModel.addHost(tag) }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add host")
                                    }
                                }
                            }
                            items@ hosts.forEachIndexed { index, host ->
                                item {
                                    HostCard(host) { updated ->
                                        viewModel.updateHost(tag, index, updated)
                                    }
                                }
                                item {
                                    Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                                        TextButton(onClick = { viewModel.removeHost(tag, index) }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Remove", color = MaterialTheme.colorScheme.error)
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
}

@Composable
private fun HostCard(host: HostDto, onChange: (HostDto) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = host.remark, onValueChange = { onChange(host.copy(remark = it)) }, label = { Text("Remark") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = host.address, onValueChange = { onChange(host.copy(address = it)) }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = host.port?.toString().orEmpty(), onValueChange = { onChange(host.copy(port = it.toIntOrNull())) }, label = { Text("Port") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = host.sni.orEmpty(), onValueChange = { onChange(host.copy(sni = it.ifBlank { null })) }, label = { Text("SNI") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = host.host.orEmpty(), onValueChange = { onChange(host.copy(host = it.ifBlank { null })) }, label = { Text("Host header") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = host.path.orEmpty(), onValueChange = { onChange(host.copy(path = it.ifBlank { null })) }, label = { Text("Path") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HostSecurity.entries.forEach { sec ->
                    FilterChip(
                        selected = host.security == sec,
                        onClick = { onChange(host.copy(security = sec)) },
                        label = { Text(when (sec) {
                            HostSecurity.InboundDefault -> "default"
                            HostSecurity.None -> "none"
                            HostSecurity.Tls -> "tls"
                        }) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = host.isDisabled == true, onCheckedChange = { onChange(host.copy(isDisabled = it)) })
                Spacer(Modifier.width(8.dp))
                Text("Disabled")
                Spacer(Modifier.width(16.dp))
                Switch(checked = host.muxEnable == true, onCheckedChange = { onChange(host.copy(muxEnable = it)) })
                Spacer(Modifier.width(8.dp))
                Text("Mux")
                Spacer(Modifier.width(16.dp))
                Switch(checked = host.allowInsecure == true, onCheckedChange = { onChange(host.copy(allowInsecure = it)) })
                Spacer(Modifier.width(8.dp))
                Text("Allow insecure")
            }
        }
    }
}
