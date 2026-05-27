package dev.marzban.admin.feature.admins

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.AdminDto
import dev.marzban.admin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDetailUi(
    val admin: AdminDto,
    val usage: Long?,
)

sealed interface AdminAction {
    data class Toast(val message: String) : AdminAction
    data object Deleted : AdminAction
}

@HiltViewModel
class AdminDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AdminRepository,
) : ViewModel() {
    private val username: String = checkNotNull(savedStateHandle["username"])

    private val _state = MutableStateFlow<UiState<AdminDetailUi>>(UiState.Loading)
    val state: StateFlow<UiState<AdminDetailUi>> = _state

    private val _actions = MutableSharedFlow<AdminAction>(extraBufferCapacity = 4)
    val actions: SharedFlow<AdminAction> = _actions.asSharedFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val admins = repository.list(username = username, limit = 1)
            val usage = repository.usage(username)
            val adminFound = (admins as? ApiResult.Success)?.value?.firstOrNull { it.username == username }
            if (admins is ApiResult.Failure || adminFound == null) {
                _state.value = UiState.Error((admins as? ApiResult.Failure)?.error?.message ?: "Admin not found")
                return@launch
            }
            _state.value = UiState.Success(AdminDetailUi(adminFound, (usage as? ApiResult.Success)?.value))
        }
    }

    fun disableUsers() = act("Users disabled") { repository.disableUsers(username) }
    fun activateUsers() = act("Users activated") { repository.activateUsers(username) }

    fun resetUsage() {
        viewModelScope.launch {
            when (val r = repository.resetUsage(username)) {
                is ApiResult.Success -> {
                    _state.value = (_state.value as? UiState.Success)?.let {
                        UiState.Success(it.value.copy(admin = r.value, usage = 0L))
                    } ?: UiState.Success(AdminDetailUi(r.value, 0L))
                    _actions.emit(AdminAction.Toast("Usage reset"))
                }
                is ApiResult.Failure -> _actions.emit(AdminAction.Toast(r.error.message ?: "Failed"))
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val r = repository.delete(username)) {
                is ApiResult.Success -> _actions.emit(AdminAction.Deleted)
                is ApiResult.Failure -> _actions.emit(AdminAction.Toast(r.error.message ?: "Failed"))
            }
        }
    }

    private fun <T> act(success: String, block: suspend () -> ApiResult<T>) {
        viewModelScope.launch {
            when (val r = block()) {
                is ApiResult.Success -> _actions.emit(AdminAction.Toast(success))
                is ApiResult.Failure -> _actions.emit(AdminAction.Toast(r.error.message ?: "Failed"))
            }
        }
    }
}
