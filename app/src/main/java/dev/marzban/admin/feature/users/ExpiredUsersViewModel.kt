package dev.marzban.admin.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpiredUsersUi(
    val users: UiState<List<String>> = UiState.Loading,
    val selected: Set<String> = emptySet(),
    val deleting: Boolean = false,
)

@HiltViewModel
class ExpiredUsersViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ExpiredUsersUi())
    val state: StateFlow<ExpiredUsersUi> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init { load() }

    fun load() {
        _state.update { it.copy(users = UiState.Loading, selected = emptySet()) }
        viewModelScope.launch {
            when (val r = repository.expired()) {
                is ApiResult.Success -> _state.update { it.copy(users = UiState.Success(r.value)) }
                is ApiResult.Failure -> _state.update { it.copy(users = UiState.Error(r.error.message ?: "Error")) }
            }
        }
    }

    fun toggleSelection(username: String) {
        _state.update {
            val newSet = it.selected.toMutableSet()
            if (newSet.contains(username)) newSet.remove(username) else newSet.add(username)
            it.copy(selected = newSet)
        }
    }

    fun selectAll() {
        val list = (_state.value.users as? UiState.Success)?.value.orEmpty()
        _state.update { it.copy(selected = list.toSet()) }
    }

    fun clearSelection() {
        _state.update { it.copy(selected = emptySet()) }
    }

    /**
     * Deletes ONLY the selected usernames, one HTTP DELETE per user. We never
     * call the bulk endpoint here — that's what makes "delete N selected" safe
     * even if the panel's expired set changes between load and confirm.
     */
    fun deleteSelected() {
        val targets = _state.value.selected.toList()
        if (targets.isEmpty()) return
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            val results = targets.map { username ->
                async { username to repository.delete(username) }
            }.awaitAll()
            val ok = results.count { it.second is ApiResult.Success }
            val failed = results.size - ok
            _events.emit(
                buildString {
                    append("Deleted $ok user")
                    if (ok != 1) append("s")
                    if (failed > 0) append(" • $failed failed")
                }
            )
            _state.update { it.copy(deleting = false, selected = emptySet()) }
            load()
        }
    }

    /**
     * Bulk-delete with an EXPLICIT `expired_before = now()` guard so we never
     * accidentally hit the catch-all endpoint without a constraint. The typed
     * confirmation in the dialog is the second safety layer above this one.
     */
    fun deleteAllExpired() {
        _state.update { it.copy(deleting = true) }
        viewModelScope.launch {
            val nowIso = java.time.Instant.now().toString().substringBefore('.')
            when (val r = repository.deleteExpired(before = nowIso)) {
                is ApiResult.Success -> _events.emit("Deleted ${r.value.size} expired user(s)")
                is ApiResult.Failure -> _events.emit(r.error.message ?: "Failed")
            }
            _state.update { it.copy(deleting = false, selected = emptySet()) }
            load()
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
