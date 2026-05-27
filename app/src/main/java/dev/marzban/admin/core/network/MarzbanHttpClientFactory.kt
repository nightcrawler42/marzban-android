package dev.marzban.admin.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarzbanHttpClientFactory @Inject constructor(
    private val authInterceptor: AuthInterceptor,
    private val unauthorizedAuthenticator: UnauthorizedAuthenticator,
) {
    fun build(trustAll: Boolean): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .authenticator(unauthorizedAuthenticator)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (debugBuild()) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
                }
            )

        if (trustAll) {
            builder.sslSocketFactory(TrustAllCerts.sslSocketFactory(), TrustAllCerts.trustManager)
            builder.hostnameVerifier { _, _ -> true }
        }
        return builder.build()
    }

    private fun debugBuild(): Boolean = dev.marzban.admin.BuildConfig.DEBUG
}
