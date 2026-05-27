package dev.marzban.admin.feature.inbounds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.marzban.admin.core.ui.EmptyPlaceholder
import dev.marzban.admin.core.ui.ErrorPlaceholder
import dev.marzban.admin.core.ui.KeyValueRow
import dev.marzban.admin.core.ui.LoadingPlaceholder
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.UiState

@Composable
fun InboundsScreen(
    onBack: () -> Unit,
    viewModel: InboundsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TitleScaffold(title = "Inbounds", onBack = onBack) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingPlaceholder()
                is UiState.Error -> ErrorPlaceholder(s.message, onRetry = viewModel::load)
                is UiState.Success -> {
                    if (s.value.isEmpty()) EmptyPlaceholder("No inbounds configured.")
                    else LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        s.value.forEach { (protocol, inbounds) ->
                            item {
                                Text(protocol, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            }
                            items@ for (inbound in inbounds) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text(inbound.tag, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                            KeyValueRow("Protocol", inbound.protocol)
                                            KeyValueRow("Network", inbound.network)
                                            KeyValueRow("TLS", inbound.tls.ifBlank { "none" })
                                            KeyValueRow("Port", inbound.port?.toString() ?: "—")
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
