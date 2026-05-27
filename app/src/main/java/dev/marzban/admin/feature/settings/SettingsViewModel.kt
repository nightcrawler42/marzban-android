package dev.marzban.admin.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.auth.AuthRepository
import dev.marzban.admin.core.network.RetrofitProvider
import dev.marzban.admin.core.storage.ServerConfigStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class SettingsUi(
    val serverUrl: String = "",
    val username: String = "",
    val trustAllCerts: Boolean = false,
)

sealed interface SettingsEvent {
    data object LoggedOut : SettingsEvent
    data class Toast(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val configStore: ServerConfigStore,
    private val authRepository: AuthRepository,
    private val retrofitProvider: RetrofitProvider,
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUi())
    val ui: StateFlow<SettingsUi> = _ui

    private val _events = MutableSharedFlow<SettingsEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                configStore.serverUrl,
                configStore.lastUsername,
                configStore.trustAllCerts,
            ) { url, user, trust ->
                SettingsUi(serverUrl = url.orEmpty(), username = user.orEmpty(), trustAllCerts = trust)
            }.collect { _ui.value = it }
        }
    }

    fun setServerUrl(value: String) {
        viewModelScope.launch {
            configStore.setServerUrl(value.trim())
            retrofitProvider.invalidate()
            _events.emit(SettingsEvent.Toast("Server URL updated"))
        }
    }

    fun setTrustAllCerts(value: Boolean) {
        viewModelScope.launch {
            configStore.setTrustAllCerts(value)
            retrofitProvider.invalidate()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _events.emit(SettingsEvent.LoggedOut)
        }
    }
}
