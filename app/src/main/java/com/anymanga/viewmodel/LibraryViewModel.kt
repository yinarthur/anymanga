package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.MangaRepository
import com.anymanga.model.Manga
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _state = MutableStateFlow<LibraryState>(LibraryState.Idle)
    val state: StateFlow<LibraryState> = _state

    fun search(query: String) {
        if (query.isBlank()) {
            _state.value = LibraryState.Idle
            return
        }

        viewModelScope.launch {
            _state.value = LibraryState.Searching
            try {
                val results = repository.searchManga(query)
                if (results.isEmpty()) {
                    _state.value = LibraryState.NoResults
                } else {
                    _state.value = LibraryState.Success(results)
                }
            } catch (e: Exception) {
                _state.value = LibraryState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class LibraryState {
        object Idle : LibraryState()
        object Searching : LibraryState()
        object NoResults : LibraryState()
        data class Success(val results: List<Pair<String, Manga>>) : LibraryState()
        data class Error(val message: String) : LibraryState()
    }
}
