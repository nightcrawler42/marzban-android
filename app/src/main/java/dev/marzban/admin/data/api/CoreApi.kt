package dev.marzban.admin.data.api

import dev.marzban.admin.data.dto.CoreStatsDto
import dev.marzban.admin.data.dto.DetailResponse
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface CoreApi {

    @GET("api/core")
    suspend fun stats(): CoreStatsDto

    @GET("api/core/config")
    suspend fun getConfig(): JsonObject

    @PUT("api/core/config")
    suspend fun updateConfig(@Body body: JsonObject): JsonObject

    @POST("api/core/restart")
    suspend fun restart(): DetailResponse
}
