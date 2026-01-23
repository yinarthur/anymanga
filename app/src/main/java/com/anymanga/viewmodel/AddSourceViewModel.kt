package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.*
import com.anymanga.engine.TemplateResolver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddSourceViewModel(
    private val database: AppDatabase,
    private val updater: TemplatesUpdater,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val resolver = TemplateResolver(database.sourceDao())
    
    private val _state = MutableStateFlow(AddSourceState())
    val state: StateFlow<AddSourceState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.userRepoUrl.collect { url ->
                _state.update { it.copy(repoUrl = url ?: "") }
            }
        }
        
        viewModelScope.launch {
            database.sourceDao().getAllTemplates().collect { templates ->
                _state.update { it.copy(allTemplates = templates) }
            }
        }
    }

    fun setRepoUrl(url: String) {
        viewModelScope.launch {
            preferencesManager.setUserRepoUrl(url)
        }
    }

    fun syncRepository() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null) }
            val success = updater.syncWithDatabase(database)
            _state.update { it.copy(isSyncing = false) }
            if (!success) {
                _state.update { it.copy(error = "Sync failed. Check your URL and connection.") }
            }
        }
    }

    fun resolveSource(input: String) {
        if (input.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isResolving = true, error = null, detectedSource = null) }
            val source = resolver.resolve(input)
            _state.update { it.copy(isResolving = false, detectedSource = source) }
            if (source == null) {
                _state.update { it.copy(error = "Could not detect source type. Please check the URL.") }
            }
        }
    }

    fun addDetectedSource() {
        val source = _state.value.detectedSource ?: return
        viewModelScope.launch {
            database.sourceDao().upsertTemplates(listOf(source))
            database.sourceDao().setUserSource(
                UserSourceEntity(domain = source.domain, enabled = true)
            )
            _state.update { it.copy(detectedSource = null, addComplete = true) }
        }
    }

    fun resetAddComplete() {
        _state.update { it.copy(addComplete = false) }
    }

    data class AddSourceState(
        val repoUrl: String = "",
        val allTemplates: List<SourceTemplateEntity> = emptyList(),
        val isSyncing: Boolean = false,
        val isResolving: Boolean = false,
        val detectedSource: SourceTemplateEntity? = null,
        val error: String? = null,
        val addComplete: Boolean = false
    )
}
