package dev.marzban.admin.data.repository

import dev.marzban.admin.core.network.ApiContainer
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.network.apiCall
import dev.marzban.admin.data.dto.AdminCreateRequest
import dev.marzban.admin.data.dto.AdminDto
import dev.marzban.admin.data.dto.AdminModifyRequest
import dev.marzban.admin.data.dto.DetailResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val api: ApiContainer,
) {
    suspend fun me(): ApiResult<AdminDto> = apiCall { api.admin().me() }
    suspend fun list(offset: Int? = null, limit: Int? = null, username: String? = null): ApiResult<List<AdminDto>> =
        apiCall { api.admin().list(offset, limit, username) }
    suspend fun create(body: AdminCreateRequest): ApiResult<AdminDto> = apiCall { api.admin().create(body) }
    suspend fun update(username: String, body: AdminModifyRequest): ApiResult<AdminDto> =
        apiCall { api.admin().update(username, body) }
    suspend fun delete(username: String): ApiResult<DetailResponse> = apiCall { api.admin().delete(username) }
    suspend fun disableUsers(username: String): ApiResult<DetailResponse> =
        apiCall { api.admin().disableUsers(username) }
    suspend fun activateUsers(username: String): ApiResult<DetailResponse> =
        apiCall { api.admin().activateUsers(username) }
    suspend fun usage(username: String): ApiResult<Long> = apiCall { api.admin().usage(username) }
    suspend fun resetUsage(username: String): ApiResult<AdminDto> = apiCall { api.admin().resetUsage(username) }
}
