package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.MangaRepository
import com.anymanga.model.Chapter
import com.anymanga.model.Manga
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MangaDetailViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state

    fun loadDetails(sourceId: String, mangaUrl: String) {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            repository.getMangaDetails(sourceId, mangaUrl).onSuccess { (manga, chapters) ->
                _state.value = DetailState.Success(manga, chapters)
            }.onFailure { error ->
                _state.value = DetailState.Error(error.message ?: "Unknown error")
            }
        }
    }

    sealed class DetailState {
        object Loading : DetailState()
        data class Success(val manga: Manga, val chapters: List<Chapter>) : DetailState()
        data class Error(val message: String) : DetailState()
    }
}
