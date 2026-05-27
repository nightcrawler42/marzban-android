package dev.marzban.admin.feature.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.util.formatBytes
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.dto.UserStatus

@Composable
fun UsersScreen(
    onOpenDetail: (String) -> Unit,
    onCreate: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: UsersViewModel = hiltViewModel(),
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val users = viewModel.pagingFlow.collectAsLazyPagingItems()

    TitleScaffold(
        title = "Users",
        onBack = onBack,
        fab = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New user") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = filter.search,
                onValueChange = viewModel::setSearch,
                placeholder = { Text("Search users") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            FilterRow(filter.status, viewModel::setStatus)
            Spacer(Modifier.height(8.dp))
            if (users.itemCount == 0 && users.loadState.refresh !is androidx.paging.LoadState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No users found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        count = users.itemCount,
                        key = { idx -> users.peek(idx)?.username ?: "row-$idx" },
                    ) { index ->
                        val user = users[index] ?: return@items
                        UserRow(user, onClick = { onOpenDetail(user.username) })
                    }
                    if (users.loadState.append is androidx.paging.LoadState.Loading) {
                        item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(current: UserStatus?, onSelect: (UserStatus?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusChip("All", current == null) { onSelect(null) }
        UserStatus.entries.forEach { status ->
            StatusChip(status.label(), current == status) { onSelect(status) }
        }
    }
}

@Composable
private fun StatusChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = if (selected) {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else AssistChipDefaults.assistChipColors(),
    )
}

@Composable
private fun UserRow(user: UserResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    UserStatusBadge(user.status)
                }
                Spacer(Modifier.height(4.dp))
                val limit = user.dataLimit
                val usage = "Used ${formatBytes(user.usedTraffic)}" + (limit?.let { " / ${formatBytes(it)}" } ?: "")
                Text(usage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (limit != null && limit > 0) {
                    val progress = (user.usedTraffic.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(4.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.size(8.dp))
        }
    }
}

