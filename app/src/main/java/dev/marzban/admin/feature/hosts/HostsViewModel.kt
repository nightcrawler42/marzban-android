package dev.marzban.admin.feature.hosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.repository.SystemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostsViewModel @Inject constructor(
    private val repository: SystemRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Map<String, List<HostDto>>>>(UiState.Loading)
    val state: StateFlow<UiState<Map<String, List<HostDto>>>> = _state

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var working: MutableMap<String, MutableList<HostDto>> = mutableMapOf()
    var dirty = false
        private set

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            when (val r = repository.hosts()) {
                is ApiResult.Success -> {
                    working = r.value.mapValues { (_, v) -> v.toMutableList() }.toMutableMap()
                    _state.value = UiState.Success(snapshot())
                    dirty = false
                }
                is ApiResult.Failure -> _state.value = UiState.Error(r.error.message ?: "Error")
            }
        }
    }

    private fun snapshot(): Map<String, List<HostDto>> = working.mapValues { it.value.toList() }

    fun updateHost(tag: String, index: Int, host: HostDto) {
        working[tag]?.set(index, host)
        dirty = true
        _state.value = UiState.Success(snapshot())
    }

    fun addHost(tag: String) {
        val list = working.getOrPut(tag) { mutableListOf() }
        list.add(HostDto(remark = "new", address = ""))
        dirty = true
        _state.value = UiState.Success(snapshot())
    }

    fun removeHost(tag: String, index: Int) {
        working[tag]?.removeAt(index)
        dirty = true
        _state.value = UiState.Success(snapshot())
    }

    fun save() {
        viewModelScope.launch {
            when (val r = repository.updateHosts(snapshot())) {
                is ApiResult.Success -> {
                    working = r.value.mapValues { (_, v) -> v.toMutableList() }.toMutableMap()
                    _state.value = UiState.Success(snapshot())
                    dirty = false
                    _events.emit("Hosts saved")
                }
                is ApiResult.Failure -> _events.emit(r.error.message ?: "Failed to save")
            }
        }
    }
}
