package dev.marzban.admin.data.api

import dev.marzban.admin.data.dto.AdminCreateRequest
import dev.marzban.admin.data.dto.AdminDto
import dev.marzban.admin.data.dto.AdminModifyRequest
import dev.marzban.admin.data.dto.DetailResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApi {

    @GET("api/admin")
    suspend fun me(): AdminDto

    @POST("api/admin")
    suspend fun create(@Body body: AdminCreateRequest): AdminDto

    @PUT("api/admin/{username}")
    suspend fun update(@Path("username") username: String, @Body body: AdminModifyRequest): AdminDto

    @DELETE("api/admin/{username}")
    suspend fun delete(@Path("username") username: String): DetailResponse

    @GET("api/admins")
    suspend fun list(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("username") username: String? = null,
    ): List<AdminDto>

    @POST("api/admin/{username}/users/disable")
    suspend fun disableUsers(@Path("username") username: String): DetailResponse

    @POST("api/admin/{username}/users/activate")
    suspend fun activateUsers(@Path("username") username: String): DetailResponse

    @GET("api/admin/usage/{username}")
    suspend fun usage(@Path("username") username: String): Long

    @POST("api/admin/usage/reset/{username}")
    suspend fun resetUsage(@Path("username") username: String): AdminDto
}
