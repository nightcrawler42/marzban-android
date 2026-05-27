package dev.marzban.admin.auth

import dev.marzban.admin.core.network.ApiContainer
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.network.RetrofitProvider
import dev.marzban.admin.core.network.apiCall
import dev.marzban.admin.core.storage.ServerConfigStore
import dev.marzban.admin.core.storage.TokenStore
import dev.marzban.admin.data.dto.AdminDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthState {
    data object Unknown : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val username: String) : AuthState
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiContainer: ApiContainer,
    private val tokenStore: TokenStore,
    private val configStore: ServerConfigStore,
    private val retrofitProvider: RetrofitProvider,
) {
    private val identity = MutableStateFlow<AdminDto?>(null)

    val authState: Flow<AuthState> = combine(
        tokenStore.token,
        configStore.lastUsername,
    ) { token, username ->
        when {
            token == null -> AuthState.SignedOut
            else -> AuthState.SignedIn(username ?: "")
        }
    }

    val currentAdmin: Flow<AdminDto?> = identity.map { it }

    suspend fun login(serverUrl: String, username: String, password: String): ApiResult<AdminDto> {
        configStore.setServerUrl(serverUrl)
        retrofitProvider.invalidate()

        val token = apiCall { apiContainer.auth().token(username, password) }
        when (token) {
            is ApiResult.Failure -> return ApiResult.Failure(token.error)
            is ApiResult.Success -> tokenStore.save(token.value.accessToken)
        }
        val me = apiCall { apiContainer.admin().me() }
        if (me is ApiResult.Success) {
            identity.value = me.value
            configStore.setLastUsername(me.value.username)
        } else {
            tokenStore.clear()
        }
        return me
    }

    suspend fun refreshIdentity(): ApiResult<AdminDto> {
        val result = apiCall { apiContainer.admin().me() }
        if (result is ApiResult.Success) identity.value = result.value
        return result
    }

    fun logout() {
        tokenStore.clear()
        identity.value = null
    }
}
