package dev.marzban.admin.core.network

import dev.marzban.admin.core.storage.TokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/** Triggers a forced logout when the panel rejects our JWT. */
class UnauthorizedAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val sessionEventBus: SessionEventBus,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't loop on the token endpoint itself.
        if (response.request.url.encodedPath.endsWith("/api/admin/token")) return null
        tokenStore.clear()
        sessionEventBus.emit(SessionEvent.ForcedLogout)
        return null
    }
}
