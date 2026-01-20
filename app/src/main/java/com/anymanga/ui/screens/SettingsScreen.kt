package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.anymanga.R
import com.anymanga.data.PreferencesManager
import com.anymanga.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    
    val themeMode by preferencesManager.themeMode.collectAsState(initial = "dark")
    val readingMode by preferencesManager.readingMode.collectAsState(initial = "ltr")
    val language by preferencesManager.language.collectAsState(initial = "en")
    
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader(stringResource(R.string.general))
            }
            
            item {
                var expanded by remember { mutableStateOf(false) }
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    subtitle = if (language == "ar") "العربية" else "English",
                    onClick = { expanded = true }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            scope.launch { preferencesManager.setLanguage("en") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("العربية") },
                        onClick = {
                            scope.launch { preferencesManager.setLanguage("ar") }
                            expanded = false
                        }
                    )
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Source,
                    title = "Default Source",
                    subtitle = "None selected",
                    onClick = { /* TODO: Source picker */ }
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.appearance))
            }
            
            item {
                var expanded by remember { mutableStateOf(false) }
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.theme),
                    subtitle = when (themeMode) {
                        "light" -> "Light"
                        "dark" -> "Dark"
                        "amoled" -> "AMOLED"
                        else -> "Dark"
                    },
                    onClick = { expanded = true }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Light") },
                        onClick = {
                            scope.launch { preferencesManager.setThemeMode("light") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Dark") },
                        onClick = {
                            scope.launch { preferencesManager.setThemeMode("dark") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("AMOLED") },
                        onClick = {
                            scope.launch { preferencesManager.setThemeMode("amoled") }
                            expanded = false
                        }
                    )
                }
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.reader))
            }
            
            item {
                var expanded by remember { mutableStateOf(false) }
                SettingsItem(
                    icon = Icons.Default.MenuBook,
                    title = "Reading Mode",
                    subtitle = when (readingMode) {
                        "ltr" -> "Left to Right"
                        "rtl" -> "Right to Left"
                        "vertical" -> "Vertical"
                        "webtoon" -> "Webtoon"
                        else -> "Left to Right"
                    },
                    onClick = { expanded = true }
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Left to Right") },
                        onClick = {
                            scope.launch { preferencesManager.setReadingMode("ltr") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Right to Left") },
                        onClick = {
                            scope.launch { preferencesManager.setReadingMode("rtl") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Vertical") },
                        onClick = {
                            scope.launch { preferencesManager.setReadingMode("vertical") }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Webtoon") },
                        onClick = {
                            scope.launch { preferencesManager.setReadingMode("webtoon") }
                            expanded = false
                        }
                    )
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.ZoomIn,
                    title = "Zoom Start Position",
                    subtitle = "Automatic",
                    onClick = { /* TODO: Zoom settings */ }
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.downloads))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Folder,
                    title = "Download Location",
                    subtitle = "/storage/emulated/0/AnyManga",
                    onClick = { /* TODO: Folder picker */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Image,
                    title = "Image Quality",
                    subtitle = "High",
                    onClick = { /* TODO: Quality picker */ }
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.backup_restore))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Create Backup",
                    subtitle = "Export library and settings",
                    onClick = { /* TODO: Create backup */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Restore,
                    title = "Restore Backup",
                    subtitle = "Import from file",
                    onClick = { /* TODO: Restore backup */ }
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.advanced))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    onClick = { /* TODO: Clear cache */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear Database",
                    subtitle = "Reset all data",
                    onClick = { /* TODO: Clear database */ },
                    textColor = Color.Red
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.about))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "2.4.0",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "GitHub",
                    subtitle = "View source code",
                    onClick = { /* TODO: Open GitHub */ }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Gavel,
                    title = "Licenses",
                    subtitle = "Open source licenses",
                    onClick = { /* TODO: Show licenses */ }
                )
            }
            
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        color = Primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = Color.White
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
