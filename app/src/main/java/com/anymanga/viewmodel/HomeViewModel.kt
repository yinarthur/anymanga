package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anymanga.data.MangaRepository
import com.anymanga.data.SourceTemplateEntity
import com.anymanga.model.Manga
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MangaRepository) : ViewModel() {
    
    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val updates = repository.getLatestUpdates()
                if (updates.isEmpty()) {
                    _state.value = HomeState.Empty
                } else {
                    _state.value = HomeState.Success(updates)
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class HomeState {
        object Loading : HomeState()
        object Empty : HomeState()
        data class Success(val updates: List<Pair<SourceTemplateEntity, List<Manga>>>) : HomeState()
        data class Error(val message: String) : HomeState()
    }
}
