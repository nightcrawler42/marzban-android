package dev.marzban.admin.feature.admins

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.data.dto.AdminCreateRequest
import dev.marzban.admin.data.dto.AdminDto
import dev.marzban.admin.data.dto.AdminModifyRequest
import dev.marzban.admin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminEditUi(
    val isNew: Boolean,
    val username: String = "",
    val password: String = "",
    val isSudo: Boolean = false,
    val telegramId: String = "",
    val discordWebhook: String = "",
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
)

sealed interface AdminEditEvent {
    data class Saved(val username: String) : AdminEditEvent
}

@HiltViewModel
class AdminEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AdminRepository,
) : ViewModel() {
    private val originalUsername: String? = savedStateHandle.get<String>("username")

    private val _state = MutableStateFlow(AdminEditUi(isNew = originalUsername == null, loading = originalUsername != null))
    val state: StateFlow<AdminEditUi> = _state

    private val _events = MutableSharedFlow<AdminEditEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<AdminEditEvent> = _events.asSharedFlow()

    init {
        if (originalUsername != null) {
            viewModelScope.launch {
                when (val r = repository.list(username = originalUsername, limit = 1)) {
                    is ApiResult.Success -> r.value.firstOrNull { it.username == originalUsername }?.let { populateFrom(it) }
                        ?: _state.update { it.copy(loading = false, error = "Admin not found") }
                    is ApiResult.Failure -> _state.update { it.copy(loading = false, error = r.error.message) }
                }
            }
        }
    }

    private fun populateFrom(admin: AdminDto) {
        _state.update {
            it.copy(
                loading = false,
                username = admin.username,
                isSudo = admin.isSudo,
                telegramId = admin.telegramId?.toString().orEmpty(),
                discordWebhook = admin.discordWebhook.orEmpty(),
            )
        }
    }

    fun setUsername(v: String) = _state.update { it.copy(username = v) }
    fun setPassword(v: String) = _state.update { it.copy(password = v) }
    fun setIsSudo(v: Boolean) = _state.update { it.copy(isSudo = v) }
    fun setTelegramId(v: String) = _state.update { it.copy(telegramId = v) }
    fun setDiscordWebhook(v: String) = _state.update { it.copy(discordWebhook = v) }

    fun submit() {
        val s = _state.value
        if (s.isNew && (s.username.isBlank() || s.password.isBlank())) {
            _state.update { it.copy(error = "Username and password required") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val result: ApiResult<AdminDto> = if (s.isNew) {
                repository.create(
                    AdminCreateRequest(
                        username = s.username,
                        password = s.password,
                        isSudo = s.isSudo,
                        telegramId = s.telegramId.toLongOrNull(),
                        discordWebhook = s.discordWebhook.ifBlank { null },
                    )
                )
            } else {
                repository.update(
                    originalUsername!!,
                    AdminModifyRequest(
                        password = s.password.ifBlank { null },
                        isSudo = s.isSudo,
                        telegramId = s.telegramId.toLongOrNull(),
                        discordWebhook = s.discordWebhook.ifBlank { null },
                    )
                )
            }
            _state.update { it.copy(submitting = false) }
            when (result) {
                is ApiResult.Success -> _events.emit(AdminEditEvent.Saved(result.value.username))
                is ApiResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }
}
