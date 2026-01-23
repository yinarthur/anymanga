package com.anymanga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anymanga.data.*

class ViewModelFactory(
    private val mangaRepository: MangaRepository,
    private val templateRepository: TemplateRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val templatesUpdater: TemplatesUpdater,
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(mangaRepository) as T
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> LibraryViewModel(mangaRepository) as T
            modelClass.isAssignableFrom(MangaDetailViewModel::class.java) -> MangaDetailViewModel(mangaRepository) as T
            modelClass.isAssignableFrom(ReaderViewModel::class.java) -> ReaderViewModel(mangaRepository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(historyRepository) as T
            modelClass.isAssignableFrom(AddSourceViewModel::class.java) -> AddSourceViewModel(database, templatesUpdater, preferencesManager) as T
            modelClass.isAssignableFrom(SourceCatalogViewModel::class.java) -> SourceCatalogViewModel(templateRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(settingsRepository, templatesUpdater, database) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
