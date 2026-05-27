package dev.marzban.admin.data.api

import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.marzban.admin.data.dto.UserStatus
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class UserApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: UserApi
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UserApi::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun listUsers_parsesResponse() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "users": [
                    {
                      "username": "alice",
                      "status": "active",
                      "used_traffic": 0,
                      "lifetime_used_traffic": 0,
                      "created_at": "2025-01-01T00:00:00",
                      "proxies": {},
                      "subscription_url": "/sub/abc"
                    }
                  ],
                  "total": 1
                }
                """.trimIndent()
            )
        )
        val result = api.list(offset = 0, limit = 10)
        assertThat(result.total).isEqualTo(1)
        assertThat(result.users).hasSize(1)
        assertThat(result.users[0].username).isEqualTo("alice")
        assertThat(result.users[0].status).isEqualTo(UserStatus.Active)
    }

    @Test
    fun listUsers_sendsQueryParams() = runTest {
        server.enqueue(MockResponse().setBody("""{"users":[],"total":0}"""))
        api.list(offset = 5, limit = 25, search = "bob", status = "disabled")
        val request = server.takeRequest()
        assertThat(request.path).contains("offset=5")
        assertThat(request.path).contains("limit=25")
        assertThat(request.path).contains("search=bob")
        assertThat(request.path).contains("status=disabled")
    }

    @Test
    fun deleteExpired_returnsUsernames() = runTest {
        server.enqueue(MockResponse().setBody("""["alice","bob"]"""))
        val deleted = api.deleteExpired()
        assertThat(deleted).containsExactly("alice", "bob")
    }
}
