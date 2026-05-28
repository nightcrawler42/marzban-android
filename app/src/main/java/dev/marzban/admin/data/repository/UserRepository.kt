package dev.marzban.admin.data.repository

import dev.marzban.admin.core.network.ApiContainer
import dev.marzban.admin.core.network.ApiResult
import dev.marzban.admin.core.network.apiCall
import dev.marzban.admin.data.dto.DetailResponse
import dev.marzban.admin.data.dto.UserCreateRequest
import dev.marzban.admin.data.dto.UserModifyRequest
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.dto.UserStatus
import dev.marzban.admin.data.dto.UserUsagesResponse
import dev.marzban.admin.data.dto.UsersResponse
import dev.marzban.admin.data.dto.UsersUsagesResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiContainer,
) {
    suspend fun list(
        offset: Int? = null,
        limit: Int? = null,
        search: String? = null,
        status: UserStatus? = null,
        admin: List<String>? = null,
        sort: String? = null,
    ): ApiResult<UsersResponse> =
        apiCall { api.user().list(offset, limit, null, search, admin, status?.wire(), sort) }

    suspend fun get(username: String): ApiResult<UserResponse> = apiCall { api.user().get(username) }
    suspend fun create(body: UserCreateRequest): ApiResult<UserResponse> = apiCall { api.user().create(body) }
    suspend fun update(username: String, body: UserModifyRequest): ApiResult<UserResponse> =
        apiCall { api.user().update(username, body) }
    suspend fun delete(username: String): ApiResult<DetailResponse> = apiCall { api.user().delete(username) }
    suspend fun resetUsage(username: String): ApiResult<UserResponse> =
        apiCall { api.user().resetUsage(username) }
    suspend fun resetAll(): ApiResult<DetailResponse> = apiCall { api.user().resetAll() }
    suspend fun revokeSubscription(username: String): ApiResult<UserResponse> =
        apiCall { api.user().revokeSubscription(username) }
    suspend fun activateNextPlan(username: String): ApiResult<UserResponse> =
        apiCall { api.user().activateNextPlan(username) }
    suspend fun setOwner(username: String, adminUsername: String): ApiResult<UserResponse> =
        apiCall { api.user().setOwner(username, adminUsername) }
    suspend fun usage(username: String, start: String?, end: String?): ApiResult<UserUsagesResponse> =
        apiCall { api.user().usage(username, start, end) }
    suspend fun usageAll(start: String?, end: String?, admin: List<String>? = null): ApiResult<UsersUsagesResponse> =
        apiCall { api.user().usageAll(start, end, admin) }
    suspend fun expired(after: String? = null, before: String? = null): ApiResult<List<String>> =
        apiCall { api.user().expired(after, before) }
    suspend fun deleteExpired(after: String? = null, before: String? = null): ApiResult<List<String>> =
        apiCall { api.user().deleteExpired(after, before) }

    /**
     * Pulls every user across paginated calls. Used by the dashboard quota
     * aggregator and the limited-users cleanup list. Caps at [hardLimit] to
     * keep memory bounded on very large panels.
     */
    suspend fun listAll(
        status: UserStatus? = null,
        pageSize: Int = 500,
        hardLimit: Int = 20_000,
    ): ApiResult<List<UserResponse>> {
        val collected = mutableListOf<UserResponse>()
        var offset = 0
        while (true) {
            val res = list(offset = offset, limit = pageSize, status = status)
            val page = when (res) {
                is ApiResult.Success -> res.value
                is ApiResult.Failure -> return ApiResult.Failure(res.error)
            }
            collected += page.users
            if (page.users.size < pageSize) break
            if (collected.size >= hardLimit) break
            if (collected.size >= page.total) break
            offset += pageSize
        }
        return ApiResult.Success(collected)
    }
}
