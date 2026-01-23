package com.anymanga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.anymanga.R
import com.anymanga.model.ThemeMode
import com.anymanga.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings ?: return // Or show loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = stringResource(R.string.appearance)) {
                    ThemeSelector(
                        currentMode = settings.themeMode,
                        onModeSelected = { viewModel.setThemeMode(it) }
                    )
                    
                    LanguageSelector(
                        currentTag = settings.languageTag,
                        onLanguageSelected = { viewModel.setLanguage(it) }
                    )
                }
            }

            // Server Section
            item {
                SettingsSection(title = "Server") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Use Local Server", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Connect to localhost:3000 for live data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.useLocalServer,
                            onCheckedChange = { viewModel.setUseLocalServer(it) }
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    var serverUrl by remember(settings.serverUrl) { mutableStateOf(settings.serverUrl) }
                    
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server URL") },
                        placeholder = { Text("https://your-ngrok-url.ngrok.io/api/") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = settings.useLocalServer,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.setServerUrl(serverUrl) }) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save"
                                )
                            }
                        }
                    )
                    
                    Text(
                        text = "Use Ngrok URL for testing or deploy to Render/Railway for permanent access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Repository Section
            item {
                SettingsSection(title = stringResource(R.string.repository)) {
                    var repoUrl by remember(settings.repositoryUrl) { mutableStateOf(settings.repositoryUrl) }
                    
                    OutlinedTextField(
                        value = repoUrl,
                        onValueChange = { repoUrl = it },
                        label = { Text(stringResource(R.string.repository_url_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { viewModel.setRepositoryUrl(repoUrl) }) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = stringResource(R.string.save)
                                )
                            }
                        }
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.templates_count, uiState.templatesCount),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (uiState.isSyncing) {
                                Text(
                                    text = stringResource(R.string.syncing),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.syncNow() },
                            enabled = !uiState.isSyncing && settings.repositoryUrl.isNotBlank()
                        ) {
                            Text(stringResource(R.string.sync_now))
                        }
                    }
                }
            }

            // Advanced Section
            item {
                SettingsSection(title = stringResource(R.string.advanced)) {
                    SettingsActionItem(
                        icon = Icons.Default.Assessment,
                        title = stringResource(R.string.diagnostics),
                        subtitle = stringResource(R.string.diagnostics_subtitle),
                        onClick = onNavigateToDiagnostics
                    )
                    
                    SettingsActionItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.clear_cache),
                        subtitle = stringResource(R.string.clear_cache_subtitle),
                        onClick = { /* TODO: Implement clear */ }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = stringResource(R.string.about)) {
                    Text(
                        text = stringResource(R.string.app_name) + " v1.0.0",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.built_with_ai),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Handle feedback
    LaunchedEffect(uiState.error, uiState.successMessage) {
        // Here we could show a Snackbar if we had a ScaffoldState
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ThemeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        SettingsActionItem(
            icon = Icons.Default.Palette,
            title = "Theme",
            subtitle = currentMode.name.lowercase().capitalize(),
            onClick = { expanded = true }
        )
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.name.lowercase().capitalize()) },
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageSelector(
    currentTag: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        SettingsActionItem(
            icon = Icons.Default.Language,
            title = "Language",
            subtitle = if (currentTag == "ar") "العربية" else "English",
            onClick = { expanded = true }
        )
        
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    onLanguageSelected("en")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("العربية") },
                onClick = {
                    onLanguageSelected("ar")
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
