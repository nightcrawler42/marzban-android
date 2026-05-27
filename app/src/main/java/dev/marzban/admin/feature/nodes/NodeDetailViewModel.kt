package dev.marzban.admin.feature.nodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.NodeResponse
import dev.marzban.admin.data.dto.NodeUsageDto
import dev.marzban.admin.data.repository.NodeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NodeDetailUi(
    val node: NodeResponse,
    val usage: NodeUsageDto?,
    val certificate: String?,
)

sealed interface NodeAction {
    data class Toast(val message: String) : NodeAction
    data object Deleted : NodeAction
}

@HiltViewModel
class NodeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NodeRepository,
) : ViewModel() {
    val id: Int = checkNotNull(savedStateHandle["id"])

    private val _state = MutableStateFlow<UiState<NodeDetailUi>>(UiState.Loading)
    val state: StateFlow<UiState<NodeDetailUi>> = _state

    private val _actions = MutableSharedFlow<NodeAction>(extraBufferCapacity = 4)
    val actions: SharedFlow<NodeAction> = _actions.asSharedFlow()

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            val node = repository.get(id)
            val usage = repository.usage()
            val settings = repository.settings()
            if (node is ApiResult.Failure) {
                _state.value = UiState.Error(node.error.message ?: "Failed to load")
                return@launch
            }
            val nodeValue = (node as ApiResult.Success).value
            val nodeUsage = (usage as? ApiResult.Success)?.value?.usages?.firstOrNull { it.nodeId == id }
            val cert = (settings as? ApiResult.Success)?.value?.certificate
            _state.value = UiState.Success(NodeDetailUi(nodeValue, nodeUsage, cert))
        }
    }

    fun reconnect() {
        viewModelScope.launch {
            when (val r = repository.reconnect(id)) {
                is ApiResult.Success -> _actions.emit(NodeAction.Toast("Reconnect requested"))
                is ApiResult.Failure -> _actions.emit(NodeAction.Toast(r.error.message ?: "Failed"))
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            when (val r = repository.delete(id)) {
                is ApiResult.Success -> _actions.emit(NodeAction.Deleted)
                is ApiResult.Failure -> _actions.emit(NodeAction.Toast(r.error.message ?: "Failed"))
            }
        }
    }
}
