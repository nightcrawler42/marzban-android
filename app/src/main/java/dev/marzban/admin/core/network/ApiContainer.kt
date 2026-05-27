package dev.marzban.admin.core.network

import dev.marzban.admin.data.api.AdminApi
import dev.marzban.admin.data.api.AuthApi
import dev.marzban.admin.data.api.CoreApi
import dev.marzban.admin.data.api.NodeApi
import dev.marzban.admin.data.api.SystemApi
import dev.marzban.admin.data.api.UserApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves Retrofit services against the currently configured server URL.
 *
 * Every accessor reads the current [RetrofitProvider] state so that changing
 * the server URL or trust-all flag is picked up without restarting the app.
 */
@Singleton
class ApiContainer @Inject constructor(
    private val retrofitProvider: RetrofitProvider,
) {
    suspend fun auth(): AuthApi = retrofitProvider.current().create(AuthApi::class.java)
    suspend fun admin(): AdminApi = retrofitProvider.current().create(AdminApi::class.java)
    suspend fun user(): UserApi = retrofitProvider.current().create(UserApi::class.java)
    suspend fun system(): SystemApi = retrofitProvider.current().create(SystemApi::class.java)
    suspend fun core(): CoreApi = retrofitProvider.current().create(CoreApi::class.java)
    suspend fun node(): NodeApi = retrofitProvider.current().create(NodeApi::class.java)
}
