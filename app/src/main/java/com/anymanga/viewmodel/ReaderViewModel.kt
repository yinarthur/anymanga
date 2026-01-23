package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.MangaRepository
import com.anymanga.model.Page
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state

    fun loadPages(sourceId: String, chapterUrl: String) {
        viewModelScope.launch {
            _state.value = ReaderState.Loading
            repository.getChapterPages(sourceId, chapterUrl).onSuccess { pages ->
                _state.value = ReaderState.Success(pages)
            }.onFailure { error ->
                _state.value = ReaderState.Error(error.message ?: "Unknown error")
            }
        }
    }

    sealed class ReaderState {
        object Loading : ReaderState()
        data class Success(val pages: List<Page>) : ReaderState()
        data class Error(val message: String) : ReaderState()
    }
}
