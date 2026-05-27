package dev.marzban.admin.feature.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.NodeResponse
import dev.marzban.admin.data.dto.NodeStatus

@Composable
fun NodesScreen(
    onOpenDetail: (Int) -> Unit,
    onCreate: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: NodesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TitleScaffold(
        title = "Nodes",
        onBack = onBack,
        fab = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New node") }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(s.value, key = { it.id }) { node ->
                        NodeRow(node, onClick = { onOpenDetail(node.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeRow(node: NodeResponse, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(node.status.color(), CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(node.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${node.address}:${node.port} • API ${node.apiPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(node.status.name.lowercase(), style = MaterialTheme.typography.labelLarge)
                node.xrayVersion?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

fun NodeStatus.color(): Color = when (this) {
    NodeStatus.Connected -> dev.marzban.admin.core.ui.theme.StatusActive
    NodeStatus.Connecting -> dev.marzban.admin.core.ui.theme.StatusLimited
    NodeStatus.Error -> dev.marzban.admin.core.ui.theme.StatusExpired
    NodeStatus.Disabled -> dev.marzban.admin.core.ui.theme.StatusDisabled
}
