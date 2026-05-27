package dev.marzban.admin.feature.users

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UserAction {
    data class Toast(val message: String) : UserAction
    data object Deleted : UserAction
}

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UserRepository,
) : ViewModel() {

    private val username: String = checkNotNull(savedStateHandle["username"])

    private val _state = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)
    val state: StateFlow<UiState<UserResponse>> = _state

    private val _actions = MutableSharedFlow<UserAction>(extraBufferCapacity = 4)
    val actions: SharedFlow<UserAction> = _actions.asSharedFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repository.get(username)) {
                is ApiResult.Success -> _state.value = UiState.Success(r.value)
                is ApiResult.Failure -> _state.value = UiState.Error(r.error.message ?: "Error")
            }
        }
    }

    fun resetUsage() = act("Usage reset") { repository.resetUsage(username) }
    fun revokeSubscription() = act("Subscription revoked") { repository.revokeSubscription(username) }
    fun activateNextPlan() = act("Next plan activated") { repository.activateNextPlan(username) }
    fun setOwner(adminUsername: String) = act("Owner updated") { repository.setOwner(username, adminUsername) }

    fun delete() {
        viewModelScope.launch {
            when (val r = repository.delete(username)) {
                is ApiResult.Success -> _actions.emit(UserAction.Deleted)
                is ApiResult.Failure -> _actions.emit(UserAction.Toast(r.error.message ?: "Delete failed"))
            }
        }
    }

    private fun act(success: String, block: suspend () -> ApiResult<UserResponse>) {
        viewModelScope.launch {
            when (val r = block()) {
                is ApiResult.Success -> {
                    _state.value = UiState.Success(r.value)
                    _actions.emit(UserAction.Toast(success))
                }
                is ApiResult.Failure -> _actions.emit(UserAction.Toast(r.error.message ?: "Failed"))
            }
        }
    }
}
