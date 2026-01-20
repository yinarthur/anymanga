package com.anymanga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.anymanga.ui.theme.AnyMangaTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.anymanga.data.PreferencesManager
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule template synchronization
        com.anymanga.sync.TemplateSyncWorker.schedule(this)

        setContent {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            
            val themeMode by preferencesManager.themeMode.collectAsState(initial = "dark")
            val language by preferencesManager.language.collectAsState(initial = "en")
            
            // Set locale
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            AnyMangaTheme(
                themeMode = themeMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    com.anymanga.ui.navigation.AppNavigation()
                }
            }
        }
    }
}
