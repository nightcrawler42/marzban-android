package dev.marzban.admin.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.serverPrefs by preferencesDataStore(name = "marzban_server")

@Singleton
class ServerConfigStore @Inject constructor(
    private val context: Context
) {
    private val keyServerUrl = stringPreferencesKey("server_url")
    private val keyTrustAllCerts = booleanPreferencesKey("trust_all_certs")
    private val keyLastUsername = stringPreferencesKey("last_username")
    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyThemeSource = stringPreferencesKey("theme_source")

    val serverUrl: Flow<String?> = context.serverPrefs.data.map { it[keyServerUrl] }
    val trustAllCerts: Flow<Boolean> = context.serverPrefs.data.map { it[keyTrustAllCerts] ?: false }
    val lastUsername: Flow<String?> = context.serverPrefs.data.map { it[keyLastUsername] }
    val themeMode: Flow<String> = context.serverPrefs.data.map { it[keyThemeMode] ?: "System" }
    val themeSource: Flow<String> = context.serverPrefs.data.map { it[keyThemeSource] ?: "Brand" }

    suspend fun setServerUrl(url: String) {
        context.serverPrefs.edit { it[keyServerUrl] = url.trimEnd('/') }
    }

    suspend fun setTrustAllCerts(value: Boolean) {
        context.serverPrefs.edit { it[keyTrustAllCerts] = value }
    }

    suspend fun setLastUsername(username: String) {
        context.serverPrefs.edit { it[keyLastUsername] = username }
    }

    suspend fun setThemeMode(value: String) {
        context.serverPrefs.edit { it[keyThemeMode] = value }
    }

    suspend fun setThemeSource(value: String) {
        context.serverPrefs.edit { it[keyThemeSource] = value }
    }

    suspend fun clear() {
        context.serverPrefs.edit { it.clear() }
    }
}
