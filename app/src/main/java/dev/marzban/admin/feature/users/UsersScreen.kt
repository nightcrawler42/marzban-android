package dev.marzban.admin.feature.users

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import dev.marzban.admin.core.ui.EmptyState
import dev.marzban.admin.core.ui.StatusPill
import dev.marzban.admin.core.ui.TitleScaffold
import dev.marzban.admin.core.ui.pill
import dev.marzban.admin.core.ui.safeBottomPadding
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
                text = { Text("New user") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SearchBar(
                value = filter.search,
                onValueChange = viewModel::setSearch,
            )
            FilterRow(filter.status, viewModel::setStatus)
            Spacer(Modifier.height(4.dp))

            val isLoadingFirstPage = users.itemCount == 0 &&
                users.loadState.refresh is androidx.paging.LoadState.Loading
            val isEmpty = users.itemCount == 0 &&
                users.loadState.refresh !is androidx.paging.LoadState.Loading

            when {
                isLoadingFirstPage -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                isEmpty -> EmptyState(
                    title = "No users found",
                    subtitle = if (filter.search.isNotBlank() || filter.status != null)
                        "Try clearing the search or status filter."
                    else "Tap “New user” to create the first user.",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = safeBottomPadding() + 96.dp, // clear FAB + nav
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        count = users.itemCount,
                        key = { idx -> users.peek(idx)?.username ?: "row-$idx" },
                    ) { index ->
                        val user = users[index] ?: return@items
                        UserRow(user, onClick = { onOpenDetail(user.username) })
                    }
                    if (users.loadState.append is androidx.paging.LoadState.Loading) {
                        item {
                            LinearProgressIndicator(
                                Modifier.fillMaxWidth().padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Search users") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun FilterRow(current: UserStatus?, onSelect: (UserStatus?) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = current == null,
                onClick = { onSelect(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
        items(UserStatus.entries) { status ->
            FilterChip(
                selected = current == status,
                onClick = { onSelect(status) },
                label = { Text(status.label()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun UserRow(user: UserResponse, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(user.username)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusPill(tone = user.status.pill())
                }
                Spacer(Modifier.height(6.dp))
                val limit = user.dataLimit
                val usage = "Used ${formatBytes(user.usedTraffic)}" +
                    (limit?.let { " / ${formatBytes(it)}" } ?: "")
                Text(
                    usage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (limit != null && limit > 0) {
                    val progress = (user.usedTraffic.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(6.dp),
                        trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

@Composable
private fun Avatar(username: String) {
    val initial = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val tint = MaterialTheme.colorScheme.primaryContainer
    val onTint = MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(tint, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initial,
            color = onTint,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
