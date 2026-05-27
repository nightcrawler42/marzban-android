package dev.marzban.admin.data.api

import dev.marzban.admin.data.dto.DetailResponse
import dev.marzban.admin.data.dto.NodeCreateRequest
import dev.marzban.admin.data.dto.NodeModifyRequest
import dev.marzban.admin.data.dto.NodeResponse
import dev.marzban.admin.data.dto.NodeSettingsResponse
import dev.marzban.admin.data.dto.NodesUsageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NodeApi {

    @GET("api/node/settings")
    suspend fun settings(): NodeSettingsResponse

    @POST("api/node")
    suspend fun create(@Body body: NodeCreateRequest): NodeResponse

    @GET("api/node/{id}")
    suspend fun get(@Path("id") id: Int): NodeResponse

    @PUT("api/node/{id}")
    suspend fun update(@Path("id") id: Int, @Body body: NodeModifyRequest): NodeResponse

    @DELETE("api/node/{id}")
    suspend fun delete(@Path("id") id: Int): DetailResponse

    @GET("api/nodes")
    suspend fun list(): List<NodeResponse>

    @POST("api/node/{id}/reconnect")
    suspend fun reconnect(@Path("id") id: Int): DetailResponse

    @GET("api/nodes/usage")
    suspend fun usage(
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
    ): NodesUsageResponse
}
