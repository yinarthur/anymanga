package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anymanga.ui.theme.*

@Composable
fun MainScreen(
    onMangaClick: (String, String) -> Unit,
    onAddSourceClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundDark.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
                val items = listOf(
                    NavigationItem("Library", "library_tab", Icons.Default.LibraryBooks),
                    NavigationItem("Updates", "updates_tab", Icons.Default.Update),
                    NavigationItem("History", "history_tab", Icons.Default.History),
                    NavigationItem("Browse", "browse_tab", Icons.Default.Explore),
                    NavigationItem("More", "more_tab", Icons.Default.MoreHoriz)
                )

                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.title,
                                modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                item.title, 
                                style = if (selected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray,
                            indicatorColor = Primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "library_tab",
            modifier = Modifier.padding(padding)
        ) {
            composable("library_tab") { LibraryScreen(onMangaClick = onMangaClick) }
            composable("updates_tab") { HomeScreen(onMangaClick = onMangaClick) }
            composable("history_tab") { HistoryScreen(onMangaClick = onMangaClick) }
            composable("browse_tab") { 
                SourcesCatalogScreen(
                    navController = navController,
                    onAddSourceClick = onAddSourceClick 
                ) 
            }
            composable("more_tab") { SettingsScreen(onBack = { navController.navigate("library_tab") }) }
        }
    }
}

data class NavigationItem(val title: String, val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
