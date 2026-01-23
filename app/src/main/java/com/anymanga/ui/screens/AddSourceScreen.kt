package com.anymanga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import com.anymanga.data.*
import com.anymanga.engine.TemplateResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceScreen(
    navController: NavController,
    viewModelFactory: com.anymanga.viewmodel.ViewModelFactory
) {
    val viewModel: com.anymanga.viewmodel.AddSourceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsState()
    
    var urlInput by remember { mutableStateOf("") }
    var showRepoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.addComplete) {
        if (state.addComplete) {
            navController.popBackStack()
            viewModel.resetAddComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Source") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.repoUrl.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.syncRepository() },
                            enabled = !state.isSyncing
                        ) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Add, contentDescription = "Sync Repository", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Repository Management Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Extension Repository",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (state.repoUrl.isNotBlank()) {
                            TextButton(
                                onClick = { viewModel.syncRepository() },
                                enabled = !state.isSyncing
                            ) {
                                Text("Sync Now")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.repoUrl.isBlank()) "No repository configured" else "Current: ...${state.repoUrl.takeLast(30)}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showRepoDialog = true }) {
                        Text(if (state.repoUrl.isBlank()) "Add Repository" else "Change Repository")
                    }
                }
            }
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            Text("Add Single Source", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Site URL or Name") },
                placeholder = { Text("example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.resolveSource(urlInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = urlInput.isNotBlank() && !state.isResolving
            ) {
                if (state.isResolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Resolve Source")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            state.detectedSource?.let { source ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Detected: ${source.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Engine: ${source.engineType}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Domain: ${source.domain}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { viewModel.addDetectedSource() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to My Sources")
                        }
                    }
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Available Sources List
            Text(
                text = "Available Sources",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val filteredTemplates = remember(state.allTemplates, urlInput) {
                if (urlInput.isBlank()) state.allTemplates
                else state.allTemplates.filter { 
                    it.name.contains(urlInput, ignoreCase = true) || it.domain.contains(urlInput, ignoreCase = true)
                }
            }

            if (filteredTemplates.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (state.allTemplates.isEmpty()) "Sync your repository to see sources" else "No sources found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTemplates) { source ->
                        Card(
                            onClick = {
                                if (!state.isResolving) {
                                    urlInput = source.domain
                                    viewModel.resolveSource(source.domain)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isResolving
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = source.name, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "${source.engineType} • ${source.lang}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.Add, contentDescription = "Select")
                            }
                        }
                    }
                }
            }
        }
        
        if (showRepoDialog) {
            var tempUrl by remember { mutableStateOf(state.repoUrl) }
            AlertDialog(
                onDismissRequest = { showRepoDialog = false },
                title = { Text("Set Repository URL") },
                text = {
                    Column {
                        Text("Enter the direct URL to a 'templates.json' file.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            label = { Text("URL") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setRepoUrl(tempUrl)
                        showRepoDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showRepoDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
