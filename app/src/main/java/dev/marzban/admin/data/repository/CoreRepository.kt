package dev.marzban.admin.data.repository

import dev.marzban.admin.core.network.ApiContainer
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.network.apiCall
import dev.marzban.admin.data.dto.CoreStatsDto
import dev.marzban.admin.data.dto.DetailResponse
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreRepository @Inject constructor(
    private val api: ApiContainer,
) {
    suspend fun stats(): ApiResult<CoreStatsDto> = apiCall { api.core().stats() }
    suspend fun getConfig(): ApiResult<JsonObject> = apiCall { api.core().getConfig() }
    suspend fun updateConfig(body: JsonObject): ApiResult<JsonObject> = apiCall { api.core().updateConfig(body) }
    suspend fun restart(): ApiResult<DetailResponse> = apiCall { api.core().restart() }
}
