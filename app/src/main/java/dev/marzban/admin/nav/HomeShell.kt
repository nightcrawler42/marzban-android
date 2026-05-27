package dev.marzban.admin.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.marzban.admin.feature.dashboard.DashboardScreen
import dev.marzban.admin.feature.nodes.NodesScreen
import dev.marzban.admin.feature.users.UsersScreen

sealed class HomeTab(val key: String, val title: String, val icon: ImageVector) {
    data object Dashboard : HomeTab("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Users : HomeTab("users", "Users", Icons.Default.Group)
    data object Nodes : HomeTab("nodes", "Nodes", Icons.Default.Cable)
    data object More : HomeTab("more", "More", Icons.Default.Settings)
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
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab.key,
                        onClick = { current = tab.key },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.title) },
                    )
                }
            }
        }
    ) { padding ->
        when (current) {
            HomeTab.Dashboard.key -> DashboardScreen()
            HomeTab.Users.key -> UsersScreen(
                onOpenDetail = onOpenUserDetail,
                onCreate = onCreateUser,
            )
            HomeTab.Nodes.key -> NodesScreen(
                onOpenDetail = onOpenNodeDetail,
                onCreate = onCreateNode,
                onBack = {},
            )
            HomeTab.More.key -> MoreScreen(
                modifier = Modifier.padding(padding),
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
