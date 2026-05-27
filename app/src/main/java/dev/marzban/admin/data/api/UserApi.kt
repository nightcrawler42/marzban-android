package dev.marzban.admin.data.api

import dev.marzban.admin.data.dto.DetailResponse
import dev.marzban.admin.data.dto.UserCreateRequest
import dev.marzban.admin.data.dto.UserModifyRequest
import dev.marzban.admin.data.dto.UserResponse
import dev.marzban.admin.data.dto.UserUsagesResponse
import dev.marzban.admin.data.dto.UsersResponse
import dev.marzban.admin.data.dto.UsersUsagesResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @POST("api/user")
    suspend fun create(@Body body: UserCreateRequest): UserResponse

    @GET("api/user/{username}")
    suspend fun get(@Path("username") username: String): UserResponse

    @PUT("api/user/{username}")
    suspend fun update(@Path("username") username: String, @Body body: UserModifyRequest): UserResponse

    @DELETE("api/user/{username}")
    suspend fun delete(@Path("username") username: String): DetailResponse

    @GET("api/users")
    suspend fun list(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("username") username: List<String>? = null,
        @Query("search") search: String? = null,
        @Query("admin") admin: List<String>? = null,
        @Query("status") status: String? = null,
        @Query("sort") sort: String? = null,
    ): UsersResponse

    @POST("api/user/{username}/reset")
    suspend fun resetUsage(@Path("username") username: String): UserResponse

    @POST("api/users/reset")
    suspend fun resetAll(): DetailResponse

    @POST("api/user/{username}/revoke_sub")
    suspend fun revokeSubscription(@Path("username") username: String): UserResponse

    @POST("api/user/{username}/active-next")
    suspend fun activateNextPlan(@Path("username") username: String): UserResponse

    @PUT("api/user/{username}/set-owner")
    suspend fun setOwner(
        @Path("username") username: String,
        @Query("admin_username") adminUsername: String,
    ): UserResponse

    @GET("api/user/{username}/usage")
    suspend fun usage(
        @Path("username") username: String,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
    ): UserUsagesResponse

    @GET("api/users/usage")
    suspend fun usageAll(
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("admin") admin: List<String>? = null,
    ): UsersUsagesResponse

    @GET("api/users/expired")
    suspend fun expired(
        @Query("expired_after") expiredAfter: String? = null,
        @Query("expired_before") expiredBefore: String? = null,
    ): List<String>

    @DELETE("api/users/expired")
    suspend fun deleteExpired(
        @Query("expired_after") expiredAfter: String? = null,
        @Query("expired_before") expiredBefore: String? = null,
    ): List<String>
}
