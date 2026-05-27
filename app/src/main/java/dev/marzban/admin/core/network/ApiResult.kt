package dev.marzban.admin.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>()
    data class Failure(val error: ApiError) : ApiResult<Nothing>()
}

sealed class ApiError(message: String) : Exception(message) {
    class Network(cause: Throwable) : ApiError(cause.message ?: "Network error")
    class Unauthorized(message: String = "Unauthorized") : ApiError(message)
    class NotFound(message: String = "Not found") : ApiError(message)
    class BadRequest(message: String) : ApiError(message)
    class Server(val code: Int, message: String) : ApiError(message)
    class Unknown(cause: Throwable) : ApiError(cause.message ?: "Unknown error")
}

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: HttpException) {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
        val detail = parseDetail(body) ?: e.message().orEmpty().ifBlank { "HTTP ${e.code()}" }
        ApiResult.Failure(
            when (e.code()) {
                400, 409, 422 -> ApiError.BadRequest(detail)
                401, 403 -> ApiError.Unauthorized(detail)
                404 -> ApiError.NotFound(detail)
                else -> ApiError.Server(e.code(), detail)
            }
        )
    } catch (io: IOException) {
        ApiResult.Failure(ApiError.Network(io))
    } catch (t: Throwable) {
        ApiResult.Failure(ApiError.Unknown(t))
    }
}

private fun parseDetail(body: String): String? {
    if (body.isBlank()) return null
    return runCatching {
        when (val element = errorJson.parseToJsonElement(body)) {
            is JsonObject -> {
                val detail = element["detail"] ?: element["message"]
                detail?.let { it.jsonPrimitive.content }
            }
            else -> null
        }
    }.getOrNull()
}
