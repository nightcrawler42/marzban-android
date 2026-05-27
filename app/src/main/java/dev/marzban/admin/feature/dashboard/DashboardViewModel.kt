package dev.marzban.admin.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.ui.UiState
import dev.marzban.admin.data.dto.CoreStatsDto
import dev.marzban.admin.data.dto.SystemStatsDto
import dev.marzban.admin.data.repository.CoreRepository
import dev.marzban.admin.data.repository.SystemRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val system: SystemStatsDto?,
    val core: CoreStatsDto?,
)

enum class RefreshInterval(val seconds: Int, val label: String) {
    Off(0, "Off"),
    Sec5(5, "5s"),
    Sec10(10, "10s"),
    Sec30(30, "30s"),
    Sec60(60, "1m"),
}

data class DashboardUi(
    val data: UiState<DashboardData> = UiState.Loading,
    val interval: RefreshInterval = RefreshInterval.Sec10,
    val refreshing: Boolean = false,
    val lastUpdated: Long? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemRepository: SystemRepository,
    private val coreRepository: CoreRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUi())
    val state: StateFlow<DashboardUi> = _state.asStateFlow()

    private var autoRefreshJob: Job? = null

    init {
        refresh()
        scheduleAutoRefresh()
    }

    fun refresh() {
        _state.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            val (sys, core) = awaitAll(
                async { systemRepository.stats() },
                async { coreRepository.stats() },
            )
            @Suppress("UNCHECKED_CAST")
            val sysRes = sys as ApiResult<SystemStatsDto>
            @Suppress("UNCHECKED_CAST")
            val coreRes = core as ApiResult<CoreStatsDto>
            val firstError = (sysRes as? ApiResult.Failure)?.error ?: (coreRes as? ApiResult.Failure)?.error
            _state.update { current ->
                if (firstError != null && current.data !is UiState.Success) {
                    current.copy(data = UiState.Error(firstError.message ?: "Error"), refreshing = false)
                } else if (sysRes is ApiResult.Success && coreRes is ApiResult.Success) {
                    current.copy(
                        data = UiState.Success(DashboardData(sysRes.value, coreRes.value)),
                        refreshing = false,
                        lastUpdated = System.currentTimeMillis(),
                    )
                } else {
                    current.copy(refreshing = false)
                }
            }
        }
    }

    fun setInterval(interval: RefreshInterval) {
        _state.update { it.copy(interval = interval) }
        scheduleAutoRefresh()
    }

    private fun scheduleAutoRefresh() {
        autoRefreshJob?.cancel()
        val seconds = _state.value.interval.seconds
        if (seconds <= 0) return
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(seconds * 1000L)
                if (!_state.value.refreshing) refresh()
            }
        }
    }

    override fun onCleared() {
        autoRefreshJob?.cancel()
        super.onCleared()
    }
}
