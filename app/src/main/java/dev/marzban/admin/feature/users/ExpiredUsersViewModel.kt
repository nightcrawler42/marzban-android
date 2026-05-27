package dev.marzban.admin.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpiredUsersViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val state: StateFlow<UiState<List<String>>> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repository.expired()) {
                is ApiResult.Success -> _state.value = UiState.Success(r.value)
                is ApiResult.Failure -> _state.value = UiState.Error(r.error.message ?: "Error")
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            when (val r = repository.deleteExpired()) {
                is ApiResult.Success -> {
                    _events.emit("Deleted ${r.value.size} expired users")
                    load()
                }
                is ApiResult.Failure -> _events.emit(r.error.message ?: "Failed")
            }
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            when (val r = repository.resetAll()) {
                is ApiResult.Success -> _events.emit("All user usages reset")
                is ApiResult.Failure -> _events.emit(r.error.message ?: "Failed")
            }
        }
    }
}
