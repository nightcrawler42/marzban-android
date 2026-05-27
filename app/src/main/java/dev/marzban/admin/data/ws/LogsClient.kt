package dev.marzban.admin.data.ws

import dev.marzban.admin.core.network.RetrofitProvider
import dev.marzban.admin.core.storage.TokenStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LogEvent {
    data class Line(val text: String) : LogEvent
    data class Closed(val reason: String?) : LogEvent
    data class Error(val cause: Throwable) : LogEvent
}

@Singleton
class LogsClient @Inject constructor(
    private val retrofitProvider: RetrofitProvider,
    private val tokenStore: TokenStore,
) {

    fun coreLogs(intervalSeconds: Int = 1): Flow<LogEvent> = open("api/core/logs", intervalSeconds)

    fun nodeLogs(nodeId: Int, intervalSeconds: Int = 1): Flow<LogEvent> =
        open("api/node/$nodeId/logs", intervalSeconds)

    private fun open(path: String, intervalSeconds: Int): Flow<LogEvent> = callbackFlow {
        val client = retrofitProvider.okHttp()
        val baseHttp = retrofitProvider.baseUrl().toHttpUrlOrNull()
            ?: error("Server URL is not configured")
        val wsScheme = if (baseHttp.scheme == "https") "wss" else "ws"
        val wsUrl = baseHttp.newBuilder()
            .scheme(wsScheme.takeIf { it == "wss" } ?: "ws")
            .addPathSegments(path)
            .addQueryParameter("interval", intervalSeconds.toString())
            .apply {
                tokenStore.current()?.let { addQueryParameter("token", it) }
            }
            .build()

        val request = Request.Builder().url(wsUrl).build()
        val socket: WebSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                trySend(LogEvent.Line(text))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                trySend(LogEvent.Closed(reason))
                close()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                trySend(LogEvent.Error(t))
                close(t)
            }
        })
        awaitClose { socket.close(1000, null) }
    }
}
