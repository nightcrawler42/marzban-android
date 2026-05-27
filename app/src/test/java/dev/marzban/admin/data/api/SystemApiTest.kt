package dev.marzban.admin.data.api

import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.marzban.admin.data.dto.HostDto
import dev.marzban.admin.data.dto.HostSecurity
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class SystemApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: SystemApi
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = false }

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SystemApi::class.java)
    }

    @After fun tearDown() { server.shutdown() }

    @Test
    fun stats_parsesAllFields() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "version":"0.8.4",
                  "mem_total":1000000,"mem_used":500000,
                  "cpu_cores":4,"cpu_usage":12.5,
                  "total_user":42,"online_users":3,
                  "users_active":30,"users_on_hold":2,"users_disabled":5,"users_expired":3,"users_limited":2,
                  "incoming_bandwidth":100,"outgoing_bandwidth":200,
                  "incoming_bandwidth_speed":10,"outgoing_bandwidth_speed":20
                }
                """.trimIndent()
            )
        )
        val s = api.stats()
        assertThat(s.version).isEqualTo("0.8.4")
        assertThat(s.totalUser).isEqualTo(42)
        assertThat(s.usersActive).isEqualTo(30)
        assertThat(s.cpuUsage).isWithin(0.001).of(12.5)
    }

    @Test
    fun hosts_roundtrip() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {"VLESS TCP REALITY":[
                  {"remark":"r1","address":"a.example","port":443,
                   "security":"tls","alpn":"","fingerprint":""}
                ]}
                """.trimIndent()
            )
        )
        val hosts = api.hosts()
        val list = hosts["VLESS TCP REALITY"]!!
        assertThat(list).hasSize(1)
        assertThat(list[0].address).isEqualTo("a.example")
        assertThat(list[0].security).isEqualTo(HostSecurity.Tls)

        // discard the GET request from the queue
        server.takeRequest()

        // PUT: encode our model back and check shape
        server.enqueue(MockResponse().setBody("""{"X":[{"remark":"x","address":"b.example"}]}"""))
        val sent = mapOf("X" to listOf(HostDto(remark = "x", address = "b.example")))
        api.updateHosts(sent)
        val req = server.takeRequest()
        val body = req.body.readUtf8()
        assertThat(body).contains("\"remark\":\"x\"")
        assertThat(body).contains("\"address\":\"b.example\"")
    }
}
