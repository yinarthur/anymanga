package com.anymanga

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import com.anymanga.data.AppDatabase
import com.anymanga.data.PreferencesManager
import com.anymanga.data.SettingsRepository
import com.anymanga.data.TemplatesUpdater
import com.anymanga.ui.theme.AnyMangaTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Core Dependencies
        val database = AppDatabase.getDatabase(this)
        val preferencesManager = PreferencesManager(this)
        val settingsRepository = SettingsRepository(preferencesManager, database)
        val templatesUpdater = TemplatesUpdater(this, preferencesManager)
        
        // Initialize Server Engine (for testing/diagnostics)
        val apiService = com.anymanga.network.ApiConfig.createApiService()
        val serverRepository = com.anymanga.data.ServerMangaRepository(apiService)
        val serverEngine = com.anymanga.engine.ServerEngine(serverRepository)
        com.anymanga.engine.EngineRegistry.registerEngine("SERVER", serverEngine)
        
        // Initialize Repositories
        val mangaRepository = com.anymanga.data.MangaRepository(database)
        val templateRepository = com.anymanga.data.TemplateRepository(templatesUpdater, database.sourceDao())
        val historyRepository = com.anymanga.data.HistoryRepository(database)

        // Set AppSettings for EngineRegistry
        lifecycleScope.launch {
            settingsRepository.settings.collect { settings ->
                com.anymanga.engine.EngineRegistry.appSettings = settings
            }
        }
        
        // Initialize ViewModel Factory
        val viewModelFactory = com.anymanga.viewmodel.ViewModelFactory(
            mangaRepository = mangaRepository,
            templateRepository = templateRepository,
            historyRepository = historyRepository,
            settingsRepository = settingsRepository,
            templatesUpdater = templatesUpdater,
            preferencesManager = preferencesManager,
            database = database
        )

        // Schedule template synchronization
        com.anymanga.sync.TemplateSyncWorker.schedule(this)

        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = null)
            
            settings?.let { config ->
                // Keep EngineRegistry settings in sync
                LaunchedEffect(config) {
                    com.anymanga.engine.EngineRegistry.appSettings = config
                }
                
                // Sync Server URL with ApiConfig
                LaunchedEffect(config.serverUrl) {
                    com.anymanga.network.ApiConfig.setServerUrl(config.serverUrl)
                }

                // Apply Language via AppCompatDelegate
                LaunchedEffect(config.languageTag) {
                    val locales = if (config.languageTag == "system") {
                        LocaleListCompat.getEmptyLocaleList()
                    } else {
                        LocaleListCompat.forLanguageTags(config.languageTag)
                    }
                    AppCompatDelegate.setApplicationLocales(locales)
                }

                // Ensure templates are available (load from assets if DB is empty)
                LaunchedEffect(Unit) {
                    templatesUpdater.ensureTemplatesAvailable(database)
                }

                AnyMangaTheme(
                    themeMode = config.themeMode,
                    dynamicColor = config.dynamicColorEnabled
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        com.anymanga.ui.navigation.AppNavigation(
                            viewModelFactory = viewModelFactory,
                            preferencesManager = preferencesManager
                        )
                    }
                }
            } ?: run {
                // Show a splash or loading if settings not loaded
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
