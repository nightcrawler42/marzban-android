package dev.marzban.admin.feature.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.data.dto.DataLimitResetStrategy
import dev.marzban.admin.data.dto.InboundDto
import dev.marzban.admin.data.dto.ProxyType
import dev.marzban.admin.data.dto.UserCreateRequest
import dev.marzban.admin.data.dto.UserModifyRequest
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.dto.UserStatusCreate
import dev.marzban.admin.data.dto.UserStatusModify
import dev.marzban.admin.data.repository.SystemRepository
import dev.marzban.admin.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject

data class UserEditUiState(
    val username: String = "",
    val isNew: Boolean = true,
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val dataLimitGb: String = "",
    val expireEpoch: Long? = null,
    val note: String = "",
    val onHoldDurationDays: String = "",
    val autoDeleteInDays: String = "",
    val resetStrategy: DataLimitResetStrategy = DataLimitResetStrategy.NoReset,
    val statusCreate: UserStatusCreate = UserStatusCreate.Active,
    val statusModify: UserStatusModify? = null,
    val inbounds: Map<String, List<String>> = emptyMap(),
    val availableInbounds: Map<String, List<InboundDto>> = emptyMap(),
    val error: String? = null,
)

sealed interface UserEditEvent {
    data class Saved(val username: String) : UserEditEvent
    data class Failure(val message: String) : UserEditEvent
}

@HiltViewModel
class UserEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val systemRepository: SystemRepository,
) : ViewModel() {

    private val originalUsername: String? = savedStateHandle.get<String>("username")

    private val _state = MutableStateFlow(
        UserEditUiState(
            isNew = originalUsername == null,
            username = originalUsername.orEmpty(),
            loading = true,
        )
    )
    val state: StateFlow<UserEditUiState> = _state

    private val _events = MutableSharedFlow<UserEditEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<UserEditEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val inbounds = when (val r = systemRepository.inbounds()) {
                is ApiResult.Success -> r.value
                else -> emptyMap()
            }
            if (originalUsername != null) {
                when (val r = userRepository.get(originalUsername)) {
                    is ApiResult.Success -> populateFrom(r.value, inbounds)
                    is ApiResult.Failure -> _state.update { it.copy(loading = false, error = r.error.message) }
                }
            } else {
                _state.update {
                    it.copy(
                        availableInbounds = inbounds,
                        inbounds = inbounds.mapValues { (_, list) -> list.map(InboundDto::tag) },
                        loading = false,
                    )
                }
            }
        }
    }

    private fun populateFrom(user: UserResponse, inbounds: Map<String, List<InboundDto>>) {
        _state.update {
            it.copy(
                username = user.username,
                loading = false,
                dataLimitGb = user.dataLimit?.let { bytes -> (bytes / 1_073_741_824.0).toString() } ?: "",
                expireEpoch = user.expire,
                note = user.note.orEmpty(),
                onHoldDurationDays = user.onHoldExpireDuration?.let { s -> (s / 86_400).toString() } ?: "",
                autoDeleteInDays = user.autoDeleteInDays?.toString() ?: "",
                resetStrategy = user.dataLimitResetStrategy,
                statusModify = when (user.status) {
                    dev.marzban.admin.data.dto.UserStatus.Active -> UserStatusModify.Active
                    dev.marzban.admin.data.dto.UserStatus.Disabled -> UserStatusModify.Disabled
                    dev.marzban.admin.data.dto.UserStatus.OnHold -> UserStatusModify.OnHold
                    else -> null
                },
                inbounds = user.inbounds,
                availableInbounds = inbounds,
            )
        }
    }

    fun setUsername(value: String) = _state.update { it.copy(username = value) }
    fun setDataLimitGb(value: String) = _state.update { it.copy(dataLimitGb = value) }
    fun setExpireEpoch(value: Long?) = _state.update { it.copy(expireEpoch = value) }
    fun setNote(value: String) = _state.update { it.copy(note = value) }
    fun setOnHoldDurationDays(value: String) = _state.update { it.copy(onHoldDurationDays = value) }
    fun setAutoDeleteInDays(value: String) = _state.update { it.copy(autoDeleteInDays = value) }
    fun setResetStrategy(value: DataLimitResetStrategy) = _state.update { it.copy(resetStrategy = value) }
    fun setStatusCreate(value: UserStatusCreate) = _state.update { it.copy(statusCreate = value) }
    fun setStatusModify(value: UserStatusModify) = _state.update { it.copy(statusModify = value) }

    fun toggleInbound(protocol: String, tag: String) {
        _state.update { st ->
            val current = st.inbounds[protocol].orEmpty().toMutableList()
            if (current.contains(tag)) current.remove(tag) else current.add(tag)
            st.copy(inbounds = st.inbounds.toMutableMap().apply { put(protocol, current) })
        }
    }

    fun submit() {
        val s = _state.value
        if (s.isNew && s.username.isBlank()) {
            _state.update { it.copy(error = "Username required") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val dataLimitBytes = s.dataLimitGb.toDoubleOrNull()?.let { (it * 1_073_741_824).toLong() }
            val onHoldDurationSeconds = s.onHoldDurationDays.toLongOrNull()?.let { it * 86_400 }
            val autoDelete = s.autoDeleteInDays.toIntOrNull()

            val result: ApiResult<UserResponse> = if (s.isNew) {
                val proxies = proxiesFromInbounds(s.inbounds.keys)
                userRepository.create(
                    UserCreateRequest(
                        username = s.username,
                        proxies = proxies,
                        inbounds = s.inbounds,
                        expire = s.expireEpoch,
                        dataLimit = dataLimitBytes,
                        dataLimitResetStrategy = s.resetStrategy,
                        note = s.note.ifBlank { null },
                        onHoldExpireDuration = onHoldDurationSeconds,
                        autoDeleteInDays = autoDelete,
                        status = s.statusCreate,
                    )
                )
            } else {
                userRepository.update(
                    originalUsername ?: s.username,
                    UserModifyRequest(
                        inbounds = s.inbounds,
                        expire = s.expireEpoch,
                        dataLimit = dataLimitBytes,
                        dataLimitResetStrategy = s.resetStrategy,
                        note = s.note.ifBlank { null },
                        onHoldExpireDuration = onHoldDurationSeconds,
                        autoDeleteInDays = autoDelete,
                        status = s.statusModify,
                    )
                )
            }
            _state.update { it.copy(submitting = false) }
            when (result) {
                is ApiResult.Success -> _events.emit(UserEditEvent.Saved(result.value.username))
                is ApiResult.Failure -> {
                    _state.update { it.copy(error = result.error.message) }
                    _events.emit(UserEditEvent.Failure(result.error.message.orEmpty()))
                }
            }
        }
    }

    /** Marzban requires every selected protocol have settings, even if empty. */
    private fun proxiesFromInbounds(protocols: Set<String>): Map<String, kotlinx.serialization.json.JsonObject> =
        protocols.filter { ProxyType.fromWire(it) != null }
            .associateWith { buildJsonObject {} }
}
