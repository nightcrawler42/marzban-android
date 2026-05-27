package dev.marzban.admin.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiError
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.storage.ServerConfigStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val trustAllCerts: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val configStore: ServerConfigStore,
) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val savedUrl = configStore.serverUrl.first().orEmpty()
            val savedUser = configStore.lastUsername.first().orEmpty()
            val trustAll = configStore.trustAllCerts.first()
            _state.update {
                it.copy(serverUrl = savedUrl, username = savedUser, trustAllCerts = trustAll)
            }
        }
    }

    fun setServerUrl(value: String) = _state.update { it.copy(serverUrl = value, error = null) }
    fun setUsername(value: String) = _state.update { it.copy(username = value, error = null) }
    fun setPassword(value: String) = _state.update { it.copy(password = value, error = null) }
    fun setTrustAllCerts(value: Boolean) {
        _state.update { it.copy(trustAllCerts = value) }
        viewModelScope.launch { configStore.setTrustAllCerts(value) }
    }

    fun submit() {
        val s = _state.value
        if (s.serverUrl.isBlank() || !s.serverUrl.startsWith("http", ignoreCase = true)) {
            _state.update { it.copy(error = "Enter a valid http(s) server URL") }
            return
        }
        if (s.username.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(error = "Username and password are required") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(s.serverUrl.trim(), s.username, s.password)) {
                is ApiResult.Success -> _state.update { it.copy(submitting = false, success = true) }
                is ApiResult.Failure -> {
                    val message = when (val err = result.error) {
                        is ApiError.Unauthorized -> "Invalid credentials"
                        is ApiError.Network -> "Network error: ${err.message}"
                        is ApiError.Server -> "Server error (${err.code}): ${err.message}"
                        else -> err.message ?: "Login failed"
                    }
                    _state.update { it.copy(submitting = false, error = message) }
                }
            }
        }
    }
}
