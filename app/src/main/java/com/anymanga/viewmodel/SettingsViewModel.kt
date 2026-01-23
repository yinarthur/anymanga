package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.AppDatabase
import com.anymanga.data.AppSettings
import com.anymanga.data.SettingsRepository
import com.anymanga.data.TemplatesUpdater
import com.anymanga.model.ThemeMode
import com.anymanga.util.UrlValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI State for the Settings Screen
 */
data class SettingsUiState(
    val settings: AppSettings? = null,
    val templatesCount: Int = 0,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for managing application settings.
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val updater: TemplatesUpdater,
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Collect settings from repository
        repository.settings
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
            .launchIn(viewModelScope)

        // Collect template count
        repository.getTemplatesCount()
            .onEach { count ->
                _uiState.update { it.copy(templatesCount = count) }
            }
            .launchIn(viewModelScope)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch {
            repository.setLanguage(tag)
        }
    }

    fun setUseLocalServer(enabled: Boolean) {
        viewModelScope.launch {
            repository.setUseLocalServer(enabled)
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            repository.setServerUrl(url)
        }
    }

    fun setRepositoryUrl(url: String) {
        if (!UrlValidator.isValidUrl(url)) {
            _uiState.update { it.copy(error = "Invalid URL format") }
            return
        }
        viewModelScope.launch {
            repository.setRepositoryUrl(UrlValidator.normalizeUrl(url))
            _uiState.update { it.copy(error = null, successMessage = "URL updated") }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null, successMessage = null) }
            val success = updater.syncWithDatabase(database)
            _uiState.update { 
                it.copy(
                    isSyncing = false,
                    successMessage = if (success) "Synced successfully!" else null,
                    error = if (!success) "Sync failed. Check connection." else null
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
