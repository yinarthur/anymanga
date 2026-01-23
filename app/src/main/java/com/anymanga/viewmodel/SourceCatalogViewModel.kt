package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.SourceWithStatus
import com.anymanga.data.TemplateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SourceCatalogViewModel(private val repository: TemplateRepository) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedLang = MutableStateFlow("All")
    val selectedLang = _selectedLang.asStateFlow()

    val sources: StateFlow<List<SourceWithStatus>> = repository.observeTemplatesWithStatus()
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { 
                it.template.name.contains(query, ignoreCase = true) || 
                it.template.domain.contains(query, ignoreCase = true) 
            }
        }
        .combine(_selectedLang) { list, lang ->
            if (lang == "All") list
            else list.filter { it.template.lang == lang }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val languages: StateFlow<List<String>> = repository.observeAllTemplates()
        .map { list -> listOf("All") + list.map { it.lang }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedLang(lang: String) {
        _selectedLang.value = lang
    }

    fun toggleSource(domain: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.setSourceEnabled(domain, enabled)
        }
    }
}
