package dev.marzban.admin.data.api

import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit

class AuthApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: AuthApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApi::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun token_success_returnsAccessToken() = runTest {
        server.enqueue(MockResponse().setBody("""{"access_token":"xyz","token_type":"bearer"}"""))
        val resp = api.token("admin", "pass")
        assertThat(resp.accessToken).isEqualTo("xyz")
    }

    @Test
    fun token_postsFormUrlEncoded() = runTest {
        server.enqueue(MockResponse().setBody("""{"access_token":"x","token_type":"bearer"}"""))
        api.token("alice", "secret")
        val request = server.takeRequest()
        val body = request.body.readUtf8()
        assertThat(body).contains("username=alice")
        assertThat(body).contains("password=secret")
        assertThat(body).contains("grant_type=password")
        assertThat(request.getHeader("Content-Type"))
            .startsWith("application/x-www-form-urlencoded")
    }

    @Test
    fun token_401_throwsHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"Incorrect username or password"}"""))
        try {
            api.token("bad", "creds")
            fail("expected HttpException")
        } catch (e: HttpException) {
            assertThat(e.code()).isEqualTo(401)
        }
    }
}
