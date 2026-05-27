package dev.marzban.admin.nav

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.marzban.admin.core.ui.LocalBottomBarInset
import dev.marzban.admin.feature.dashboard.DashboardScreen
import dev.marzban.admin.feature.nodes.NodesScreen
import dev.marzban.admin.feature.users.UsersScreen

sealed class HomeTab(val key: String, val title: String, val icon: ImageVector) {
    data object Dashboard : HomeTab("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Users : HomeTab("users", "Users", Icons.Default.Group)
    data object Nodes : HomeTab("nodes", "Nodes", Icons.Default.Cable)
    data object More : HomeTab("more", "More", Icons.Default.MoreHoriz)
}

@Composable
fun HomeShell(
    onOpenUserDetail: (String) -> Unit,
    onCreateUser: () -> Unit,
    onOpenNodeDetail: (Int) -> Unit,
    onCreateNode: () -> Unit,
    onOpenAdmins: () -> Unit,
    onOpenExpiredUsers: () -> Unit,
    onOpenHosts: () -> Unit,
    onOpenInbounds: () -> Unit,
    onOpenCore: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var current by rememberSaveable { mutableStateOf(HomeTab.Dashboard.key) }
    val tabs = remember { listOf(HomeTab.Dashboard, HomeTab.Users, HomeTab.Nodes, HomeTab.More) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.key,
                        onClick = { current = tab.key },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.title) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    ) { padding ->
        // Hand the bottom-nav inset to every nested LazyColumn so they can scroll
        // *past* the nav bar with proper contentPadding. The Box below also pads the
        // top so non-scrolling content doesn't slip under the system bar.
        CompositionLocalProvider(LocalBottomBarInset provides padding.calculateBottomPadding()) {
            Box(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "tab",
                ) { key ->
                    when (key) {
                        HomeTab.Dashboard.key -> DashboardScreen()
                        HomeTab.Users.key -> UsersScreen(
                            onOpenDetail = onOpenUserDetail,
                            onCreate = onCreateUser,
                        )
                        HomeTab.Nodes.key -> NodesScreen(
                            onOpenDetail = onOpenNodeDetail,
                            onCreate = onCreateNode,
                            onBack = null,
                        )
                        HomeTab.More.key -> MoreScreen(
                            onOpenAdmins = onOpenAdmins,
                            onOpenExpiredUsers = onOpenExpiredUsers,
                            onOpenHosts = onOpenHosts,
                            onOpenInbounds = onOpenInbounds,
                            onOpenCore = onOpenCore,
                            onOpenBackup = onOpenBackup,
                            onOpenSettings = onOpenSettings,
                        )
                    }
                }
            }
        }
    }
}
