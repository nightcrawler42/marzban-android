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
import dev.marzban.admin.data.repository.UserRepository
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

data class UsageAggregate(
    val totalQuota: Long,
    val totalUsed: Long,
    val unlimitedCount: Int,
    val limitedCount: Int,
) {
    val remaining: Long get() = (totalQuota - totalUsed).coerceAtLeast(0L)
    val usedFraction: Float
        get() = if (totalQuota <= 0L) 0f
                else (totalUsed.toDouble() / totalQuota.toDouble()).toFloat().coerceIn(0f, 1f)
}

data class DashboardData(
    val system: SystemStatsDto?,
    val core: CoreStatsDto?,
    val usage: UsageAggregate?,
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
    private val userRepository: UserRepository,
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
            val results = awaitAll(
                async { systemRepository.stats() },
                async { coreRepository.stats() },
                async { userRepository.listAll() },
            )
            @Suppress("UNCHECKED_CAST")
            val sysRes = results[0] as ApiResult<SystemStatsDto>
            @Suppress("UNCHECKED_CAST")
            val coreRes = results[1] as ApiResult<CoreStatsDto>
            @Suppress("UNCHECKED_CAST")
            val usersRes = results[2] as ApiResult<List<dev.marzban.admin.data.dto.UserResponse>>

            val firstError = (sysRes as? ApiResult.Failure)?.error
                ?: (coreRes as? ApiResult.Failure)?.error
            // Users aggregation is best-effort — a failure there should not blank
            // the whole dashboard. We just skip the quota card when it fails.
            val aggregate = (usersRes as? ApiResult.Success)?.value?.let(::aggregate)

            _state.update { current ->
                if (firstError != null && current.data !is UiState.Success) {
                    current.copy(data = UiState.Error(firstError.message ?: "Error"), refreshing = false)
                } else if (sysRes is ApiResult.Success && coreRes is ApiResult.Success) {
                    current.copy(
                        data = UiState.Success(DashboardData(sysRes.value, coreRes.value, aggregate)),
                        refreshing = false,
                        lastUpdated = System.currentTimeMillis(),
                    )
                } else {
                    current.copy(refreshing = false)
                }
            }
        }
    }

    private fun aggregate(users: List<dev.marzban.admin.data.dto.UserResponse>): UsageAggregate {
        var quota = 0L
        var used = 0L
        var unlimited = 0
        var limited = 0
        users.forEach { u ->
            used += u.usedTraffic
            val limit = u.dataLimit
            if (limit != null && limit > 0L) {
                quota += limit
                limited += 1
            } else {
                unlimited += 1
            }
        }
        return UsageAggregate(quota, used, unlimited, limited)
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
