package com.anymanga.data

import android.content.Context
import com.anymanga.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * State class for application settings.
 */
data class AppSettings(
    val themeMode: ThemeMode,
    val languageTag: String,
    val dynamicColorEnabled: Boolean,
    val repositoryUrl: String,
    val lastSyncEpoch: Long,
    val lastEtag: String?,
    val lastError: String?,
    val templatesCount: Int,
    val useLocalServer: Boolean,
    val serverUrl: String
)

/**
 * Repository for managing application settings and preferences.
 * Acts as a wrapper around PreferencesManager with additional logic.
 */
class SettingsRepository(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) {

    val settings: Flow<AppSettings> = combine(
        preferencesManager.themeMode,
        preferencesManager.language,
        preferencesManager.userRepoUrl,
        preferencesManager.lastTemplatesUpdate,
        preferencesManager.templatesEtag,
        preferencesManager.useLocalServer,
        preferencesManager.serverUrl
    ) { flows: Array<Any?> ->
        val theme = flows[0] as String
        val lang = flows[1] as String
        val repo = flows[2] as String?
        val lastUpdate = flows[3] as Long
        val etag = flows[4] as String?
        val useServer = flows[5] as Boolean
        val serverUrl = flows[6] as String
        
        AppSettings(
            themeMode = try { ThemeMode.valueOf(theme.uppercase()) } catch (e: Exception) { ThemeMode.DARK },
            languageTag = lang,
            dynamicColorEnabled = true,
            repositoryUrl = repo ?: "",
            lastSyncEpoch = lastUpdate,
            lastEtag = etag,
            lastError = null,
            templatesCount = 0,
            useLocalServer = useServer,
            serverUrl = serverUrl
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode.name.lowercase())
    }

    suspend fun setLanguage(tag: String) {
        preferencesManager.setLanguage(tag)
    }

    suspend fun setRepositoryUrl(url: String) {
        preferencesManager.setUserRepoUrl(url)
    }

    suspend fun setUseLocalServer(enabled: Boolean) {
        preferencesManager.setUseLocalServer(enabled)
    }

    suspend fun setServerUrl(url: String) {
        preferencesManager.setServerUrl(url)
    }

    /**
     * Get count of templates from database
     */
    fun getTemplatesCount(): Flow<Int> {
        return database.sourceDao().getAllTemplates().map { it.size }
    }
}
