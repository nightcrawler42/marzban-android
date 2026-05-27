package dev.marzban.admin.data.repository

import dev.marzban.admin.core.network.ApiContainer
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.network.apiCall
import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.dto.InboundDto
import dev.marzban.admin.data.dto.SystemStatsDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    private val api: ApiContainer,
) {
    suspend fun stats(): ApiResult<SystemStatsDto> = apiCall { api.system().stats() }
    suspend fun inbounds(): ApiResult<Map<String, List<InboundDto>>> = apiCall { api.system().inbounds() }
    suspend fun hosts(): ApiResult<Map<String, List<HostDto>>> = apiCall { api.system().hosts() }
    suspend fun updateHosts(body: Map<String, List<HostDto>>): ApiResult<Map<String, List<HostDto>>> =
        apiCall { api.system().updateHosts(body) }
}
