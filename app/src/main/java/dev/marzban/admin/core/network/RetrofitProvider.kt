package dev.marzban.admin.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.marzban.admin.core.storage.ServerConfigStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

private const val PLACEHOLDER_BASE = "https://placeholder.invalid/"

/**
 * Builds Retrofit on demand against the current server URL + trust-all flag.
 * The same instance is cached as long as those inputs don't change.
 */
@Singleton
class RetrofitProvider @Inject constructor(
    private val configStore: ServerConfigStore,
    private val clientFactory: MarzbanHttpClientFactory,
    val json: Json,
) {
    private data class CacheKey(val baseUrl: String, val trustAll: Boolean)
    private data class Cached(val key: CacheKey, val client: OkHttpClient, val retrofit: Retrofit)
    private val cache = AtomicReference<Cached?>(null)

    suspend fun current(): Retrofit {
        val baseUrl = (configStore.serverUrl.first()?.trimEnd('/')?.plus("/")) ?: PLACEHOLDER_BASE
        val trustAll = configStore.trustAllCerts.first()
        val key = CacheKey(baseUrl, trustAll)
        cache.get()?.takeIf { it.key == key }?.let { return it.retrofit }

        val client = clientFactory.build(trustAll)
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        cache.set(Cached(key, client, retrofit))
        return retrofit
    }

    suspend fun okHttp(): OkHttpClient {
        current()
        return cache.get()!!.client
    }

    suspend fun baseUrl(): String = current().baseUrl().toString()

    fun invalidate() {
        cache.set(null)
    }
}
