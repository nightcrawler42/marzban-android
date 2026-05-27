package dev.marzban.admin.feature.nodes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.data.dto.NodeCreateRequest
import dev.marzban.admin.data.dto.NodeModifyRequest
import dev.marzban.admin.data.dto.NodeResponse
import dev.marzban.admin.data.repository.NodeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NodeEditUi(
    val isNew: Boolean,
    val name: String = "",
    val address: String = "",
    val port: String = "62050",
    val apiPort: String = "62051",
    val usageCoefficient: String = "1.0",
    val addAsNewHost: Boolean = true,
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
)

sealed interface NodeEditEvent { data class Saved(val id: Int) : NodeEditEvent }

@HiltViewModel
class NodeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NodeRepository,
) : ViewModel() {
    private val originalId: Int? = savedStateHandle.get<Int>("id")?.takeIf { it >= 0 }

    private val _state = MutableStateFlow(NodeEditUi(isNew = originalId == null, loading = originalId != null))
    val state: StateFlow<NodeEditUi> = _state

    private val _events = MutableSharedFlow<NodeEditEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<NodeEditEvent> = _events.asSharedFlow()

    init {
        if (originalId != null) {
            viewModelScope.launch {
                when (val r = repository.get(originalId)) {
                    is ApiResult.Success -> populate(r.value)
                    is ApiResult.Failure -> _state.update { it.copy(loading = false, error = r.error.message) }
                }
            }
        }
    }

    private fun populate(node: NodeResponse) {
        _state.update {
            it.copy(
                loading = false,
                name = node.name,
                address = node.address,
                port = node.port.toString(),
                apiPort = node.apiPort.toString(),
                usageCoefficient = node.usageCoefficient.toString(),
            )
        }
    }

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setAddress(v: String) = _state.update { it.copy(address = v) }
    fun setPort(v: String) = _state.update { it.copy(port = v) }
    fun setApiPort(v: String) = _state.update { it.copy(apiPort = v) }
    fun setUsageCoefficient(v: String) = _state.update { it.copy(usageCoefficient = v) }
    fun setAddAsNewHost(v: Boolean) = _state.update { it.copy(addAsNewHost = v) }

    fun submit() {
        val s = _state.value
        if (s.name.isBlank() || s.address.isBlank()) {
            _state.update { it.copy(error = "Name and address required") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val port = s.port.toIntOrNull() ?: 62050
            val apiPort = s.apiPort.toIntOrNull() ?: 62051
            val coef = s.usageCoefficient.toDoubleOrNull() ?: 1.0
            val result: ApiResult<NodeResponse> = if (s.isNew) {
                repository.create(
                    NodeCreateRequest(
                        name = s.name,
                        address = s.address,
                        port = port,
                        apiPort = apiPort,
                        usageCoefficient = coef,
                        addAsNewHost = s.addAsNewHost,
                    )
                )
            } else {
                repository.update(
                    originalId!!,
                    NodeModifyRequest(
                        name = s.name,
                        address = s.address,
                        port = port,
                        apiPort = apiPort,
                        usageCoefficient = coef,
                    )
                )
            }
            _state.update { it.copy(submitting = false) }
            when (result) {
                is ApiResult.Success -> _events.emit(NodeEditEvent.Saved(result.value.id))
                is ApiResult.Failure -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }
}
