package com.anymanga.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anymanga.data.PreferencesManager
import com.anymanga.ui.screens.*
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    val scope = rememberCoroutineScope()
    
    val isFirstLaunch by preferencesManager.isFirstLaunch.collectAsState(initial = true)
    
    // Determine start destination based on first launch
    val startDestination = if (isFirstLaunch) "welcome" else "main"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(onGetStarted = {
                scope.launch {
                    preferencesManager.setFirstLaunchComplete()
                }
                navController.navigate("main") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }
        composable("main") {
            MainScreen(
                onMangaClick = { mangaUrl, sourceId ->
                    val encodedUrl = URLEncoder.encode(mangaUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate("manga_detail/$sourceId/$encodedUrl")
                },
                onAddSourceClick = {
                    navController.navigate("add_source")
                }
            )
        }
        composable(
            route = "manga_detail/{sourceId}/{url}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            
            MangaDetailScreen(
                mangaUrl = url,
                sourceId = sourceId,
                onBack = { navController.popBackStack() },
                onRead = { chapter ->
                    val encodedChapterUrl = URLEncoder.encode(chapter.url, StandardCharsets.UTF_8.toString())
                    navController.navigate("reader/$sourceId/$encodedChapterUrl")
                }
            )
        }
        composable(
            route = "reader/{sourceId}/{chapterUrl}",
            arguments = listOf(
                navArgument("sourceId") { type = NavType.StringType },
                navArgument("chapterUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sourceId = backStackEntry.arguments?.getString("sourceId") ?: ""
            val encodedUrl = backStackEntry.arguments?.getString("chapterUrl") ?: ""
            val chapterUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            
            ReaderScreen(
                chapterUrl = chapterUrl,
                sourceId = sourceId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("add_source") {
            AddSourceScreen(navController = navController)
        }
    }
}
