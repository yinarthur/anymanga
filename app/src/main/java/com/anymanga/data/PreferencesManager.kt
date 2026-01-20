package com.anymanga.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val LANGUAGE = stringPreferencesKey("language")
        private val PINNED_SOURCES = stringPreferencesKey("pinned_sources")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val READING_MODE = stringPreferencesKey("reading_mode")
        private val DEFAULT_SOURCE = stringPreferencesKey("default_source")
        private val USER_REPO_URL = stringPreferencesKey("user_repo_url")
        private val TEMPLATES_ETAG = stringPreferencesKey("templates_etag")
        private val LAST_TEMPLATES_UPDATE = androidx.datastore.preferences.core.longPreferencesKey("last_templates_update")
    }

    val templatesEtag: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TEMPLATES_ETAG]
    }

    suspend fun getTemplatesEtag(): String? {
        return context.dataStore.data.map { it[TEMPLATES_ETAG] }.first()
    }

    suspend fun saveTemplatesEtag(etag: String) {
        context.dataStore.edit { preferences ->
            preferences[TEMPLATES_ETAG] = etag
        }
    }

    val lastTemplatesUpdate: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_TEMPLATES_UPDATE] ?: 0L
    }

    suspend fun saveLastTemplatesUpdate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_TEMPLATES_UPDATE] = timestamp
        }
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "dark"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    val readingMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[READING_MODE] ?: "ltr"
    }

    suspend fun setReadingMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[READING_MODE] = mode
        }
    }

    val defaultSource: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_SOURCE] ?: ""
    }

    suspend fun setDefaultSource(source: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_SOURCE] = source
        }
    }

    val userRepoUrl: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_REPO_URL]
    }

    suspend fun setUserRepoUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_REPO_URL] = url
        }
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "en"
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }

    val pinnedSources: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PINNED_SOURCES]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }

    suspend fun togglePinnedSource(sourceId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PINNED_SOURCES]?.split(",")?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()
            if (current.contains(sourceId)) {
                current.remove(sourceId)
            } else {
                current.add(sourceId)
            }
            preferences[PINNED_SOURCES] = current.joinToString(",")
        }
    }
}
