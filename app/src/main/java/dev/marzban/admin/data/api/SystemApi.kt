package dev.marzban.admin.data.api

import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.dto.InboundDto
import dev.marzban.admin.data.dto.SystemStatsDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SystemApi {

    @GET("api/system")
    suspend fun stats(): SystemStatsDto

    @GET("api/inbounds")
    suspend fun inbounds(): @JvmSuppressWildcards Map<String, List<InboundDto>>

    @GET("api/hosts")
    suspend fun hosts(): @JvmSuppressWildcards Map<String, List<HostDto>>

    @PUT("api/hosts")
    suspend fun updateHosts(
        @Body body: @JvmSuppressWildcards Map<String, List<HostDto>>
    ): @JvmSuppressWildcards Map<String, List<HostDto>>
}
