package dev.marzban.admin.data.repository

import dev.marzban.admin.core.network.ApiContainer
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.network.apiCall
import dev.marzban.admin.data.dto.DetailResponse
import dev.marzban.admin.data.dto.NodeCreateRequest
import dev.marzban.admin.data.dto.NodeModifyRequest
import dev.marzban.admin.data.dto.NodeResponse
import dev.marzban.admin.data.dto.NodeSettingsResponse
import dev.marzban.admin.data.dto.NodesUsageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeRepository @Inject constructor(
    private val api: ApiContainer,
) {
    suspend fun list(): ApiResult<List<NodeResponse>> = apiCall { api.node().list() }
    suspend fun settings(): ApiResult<NodeSettingsResponse> = apiCall { api.node().settings() }
    suspend fun get(id: Int): ApiResult<NodeResponse> = apiCall { api.node().get(id) }
    suspend fun create(body: NodeCreateRequest): ApiResult<NodeResponse> = apiCall { api.node().create(body) }
    suspend fun update(id: Int, body: NodeModifyRequest): ApiResult<NodeResponse> =
        apiCall { api.node().update(id, body) }
    suspend fun delete(id: Int): ApiResult<DetailResponse> = apiCall { api.node().delete(id) }
    suspend fun reconnect(id: Int): ApiResult<DetailResponse> = apiCall { api.node().reconnect(id) }
    suspend fun usage(start: String? = null, end: String? = null): ApiResult<NodesUsageResponse> =
        apiCall { api.node().usage(start, end) }
}
