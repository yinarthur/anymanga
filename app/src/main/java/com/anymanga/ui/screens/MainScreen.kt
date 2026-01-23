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
import androidx.compose.ui.res.stringResource
import com.anymanga.R

@Composable
fun MainScreen(
    settingsViewModel: com.anymanga.viewmodel.SettingsViewModel,
    viewModelFactory: com.anymanga.viewmodel.ViewModelFactory,
    onMangaClick: (String, String) -> Unit,
    onAddSourceClick: () -> Unit,
    onNavigateToDiagnostics: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navigationItems = listOf(
        NavigationItem(R.string.library, "library_tab", Icons.Default.LibraryBooks),
        NavigationItem(R.string.updates, "updates_tab", Icons.Default.Update),
        NavigationItem(R.string.history, "history_tab", Icons.Default.History),
        NavigationItem(R.string.browse, "browse_tab", Icons.Default.Explore),
        NavigationItem(R.string.more, "more_tab", Icons.Default.MoreHoriz)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                navigationItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    val title = stringResource(item.titleRes)
                    
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = title,
                                modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                title, 
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
                        }
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
            composable("library_tab") { LibraryScreen(onMangaClick = onMangaClick, viewModelFactory = viewModelFactory) }
            composable("updates_tab") { HomeScreen(onMangaClick = onMangaClick, viewModelFactory = viewModelFactory) }
            composable("history_tab") { HistoryScreen(onMangaClick = onMangaClick, viewModelFactory = viewModelFactory) }
            composable("browse_tab") { 
                SourcesCatalogScreen(
                    navController = navController,
                    onAddSourceClick = onAddSourceClick,
                    viewModelFactory = viewModelFactory
                ) 
            }
            composable("more_tab") { 
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.navigate("library_tab") },
                    onNavigateToDiagnostics = onNavigateToDiagnostics
                ) 
            }
        }
    }
}

data class NavigationItem(
    val titleRes: Int, 
    val route: String, 
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
