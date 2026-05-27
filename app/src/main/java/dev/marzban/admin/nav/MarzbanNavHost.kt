package dev.marzban.admin.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.marzban.admin.auth.AuthRepository
import dev.marzban.admin.auth.AuthState
import dev.marzban.admin.auth.LoginScreen
import dev.marzban.admin.core.network.SessionEvent
import dev.marzban.admin.core.network.SessionEventBus
import dev.marzban.admin.feature.admins.AdminDetailScreen
import dev.marzban.admin.feature.admins.AdminEditScreen
import dev.marzban.admin.feature.admins.AdminsScreen
import dev.marzban.admin.feature.core.CoreConfigScreen
import dev.marzban.admin.feature.core.CoreStatusScreen
import dev.marzban.admin.feature.hosts.HostsScreen
import dev.marzban.admin.feature.inbounds.InboundsScreen
import dev.marzban.admin.feature.logs.LogsScreen
import dev.marzban.admin.feature.nodes.NodeDetailScreen
import dev.marzban.admin.feature.nodes.NodeEditScreen
import dev.marzban.admin.feature.settings.SettingsScreen
import dev.marzban.admin.feature.users.ExpiredUsersScreen
import dev.marzban.admin.feature.users.UserDetailScreen
import dev.marzban.admin.feature.users.UserEditScreen
import javax.inject.Inject

@Composable
fun MarzbanNavHost(
    viewModel: NavHostViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle(AuthState.Unknown)
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.sessionEvents.collect { event ->
            if (event is SessionEvent.ForcedLogout) {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val startDestination = when (authState) {
        is AuthState.SignedIn -> "home"
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            LoginScreen(onSignedIn = {
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }

        composable("home") {
            HomeShell(
                onOpenUserDetail = { username -> navController.navigate("user/$username") },
                onCreateUser = { navController.navigate("user/edit") },
                onOpenNodeDetail = { id -> navController.navigate("node/$id") },
                onCreateNode = { navController.navigate("node/edit") },
                onOpenAdmins = { navController.navigate("admins") },
                onOpenExpiredUsers = { navController.navigate("users/expired") },
                onOpenHosts = { navController.navigate("hosts") },
                onOpenInbounds = { navController.navigate("inbounds") },
                onOpenCore = { navController.navigate("core") },
                onOpenSettings = { navController.navigate("settings") },
            )
        }

        composable(
            route = "user/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) {
            UserDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { username -> navController.navigate("user/edit?username=$username") },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable(
            route = "user/edit?username={username}",
            arguments = listOf(navArgument("username") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            UserEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable("users/expired") {
            ExpiredUsersScreen(onBack = { navController.popBackStack() })
        }

        composable("admins") {
            AdminsScreen(
                onOpenDetail = { username -> navController.navigate("admin/$username") },
                onCreate = { navController.navigate("admin/edit") },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "admin/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) {
            AdminDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { username -> navController.navigate("admin/edit?username=$username") },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(
            route = "admin/edit?username={username}",
            arguments = listOf(navArgument("username") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AdminEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = "node/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            NodeDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("node/edit?id=$id") },
                onLogs = { id -> navController.navigate("node/$id/logs") },
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(
            route = "node/edit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            NodeEditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = "node/{nodeId}/logs",
            arguments = listOf(navArgument("nodeId") { type = NavType.IntType })
        ) {
            LogsScreen(onBack = { navController.popBackStack() })
        }

        composable("hosts") {
            HostsScreen(onBack = { navController.popBackStack() })
        }
        composable("inbounds") {
            InboundsScreen(onBack = { navController.popBackStack() })
        }
        composable("core") {
            CoreStatusScreen(
                onBack = { navController.popBackStack() },
                onConfig = { navController.navigate("core/config") },
                onLogs = { navController.navigate("core/logs") },
            )
        }
        composable("core/config") {
            CoreConfigScreen(onBack = { navController.popBackStack() })
        }
        composable("core/logs") {
            LogsScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class NavHostViewModel @Inject constructor(
    authRepository: AuthRepository,
    sessionEventBus: SessionEventBus,
) : androidx.lifecycle.ViewModel() {
    val authState = authRepository.authState
    val sessionEvents = sessionEventBus.events
}
