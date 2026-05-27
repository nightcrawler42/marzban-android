package dev.marzban.admin.nav

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object Dashboard : Route
    @Serializable data object Users : Route
    @Serializable data class UserDetail(val username: String) : Route
    @Serializable data class UserEdit(val username: String? = null) : Route
    @Serializable data object ExpiredUsers : Route
    @Serializable data object Admins : Route
    @Serializable data class AdminDetail(val username: String) : Route
    @Serializable data class AdminEdit(val username: String? = null) : Route
    @Serializable data object Nodes : Route
    @Serializable data class NodeDetail(val id: Int) : Route
    @Serializable data class NodeEdit(val id: Int? = null) : Route
    @Serializable data class NodeLogs(val id: Int) : Route
    @Serializable data object Hosts : Route
    @Serializable data object Inbounds : Route
    @Serializable data object CoreStatus : Route
    @Serializable data object CoreConfig : Route
    @Serializable data object CoreLogs : Route
    @Serializable data object Settings : Route
}
