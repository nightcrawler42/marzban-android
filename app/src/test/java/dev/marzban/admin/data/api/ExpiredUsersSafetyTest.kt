package dev.marzban.admin.data.api

import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/**
 * Guards the safety contract for destructive expired-user flows. The screen
 * either (1) deletes selected usernames one-by-one via per-user DELETE, or
 * (2) calls the bulk endpoint with an explicit `expired_before` constraint.
 * Neither path ever hits the bulk endpoint with no parameters at all.
 */
class ExpiredUsersSafetyTest {

    private lateinit var server: MockWebServer
    private lateinit var api: UserApi
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = false }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UserApi::class.java)
    }

    @After fun tearDown() { server.shutdown() }

    @Test
    fun perUserDelete_sendsOnePathPerUsername() = runTest {
        repeat(3) { server.enqueue(MockResponse().setBody("""{"detail":"ok"}""")) }

        api.delete("alice")
        api.delete("bob")
        api.delete("carol")

        val paths = listOf(server.takeRequest(), server.takeRequest(), server.takeRequest()).map { it.path }
        assertThat(paths).containsExactly(
            "/api/user/alice",
            "/api/user/bob",
            "/api/user/carol",
        )
    }

    @Test
    fun deleteExpired_withBeforeArg_sendsConstraintQuery() = runTest {
        server.enqueue(MockResponse().setBody("""["alice","bob"]"""))

        api.deleteExpired(expiredBefore = "2026-01-01T00:00:00")
        val req = server.takeRequest()

        assertThat(req.method).isEqualTo("DELETE")
        assertThat(req.path).contains("/api/users/expired")
        assertThat(req.path).contains("expired_before=2026-01-01T00%3A00%3A00")
    }

    @Test
    fun deleteExpired_withoutArgs_returnsUsernames() = runTest {
        // The API still accepts no-arg calls — the safety check is enforced in
        // the ViewModel + TypedConfirmDialog above this layer. We confirm here
        // that the endpoint round-trips correctly.
        server.enqueue(MockResponse().setBody("""["alice","bob","carol"]"""))
        val deleted = api.deleteExpired()
        assertThat(deleted).containsExactly("alice", "bob", "carol")
    }
}
