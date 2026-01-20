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
import androidx.navigation.NavController
import com.anymanga.data.PreferencesManager
import com.anymanga.data.AppDatabase
import com.anymanga.engine.TemplateResolver
import com.anymanga.data.SourceTemplateEntity
import com.anymanga.data.UserSourceEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val resolver = remember { TemplateResolver(database.sourceDao()) }
    val preferencesManager = remember { PreferencesManager(context) }

    var urlInput by remember { mutableStateOf("") }
    var detectedSource by remember { mutableStateOf<SourceTemplateEntity?>(null) }
    var isResolving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Repository State
    var showRepoDialog by remember { mutableStateOf(false) }
    var currentRepoUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentRepoUrl = preferencesManager.userRepoUrl.first() ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Source") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    Text(
                        text = "Extension Repository",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentRepoUrl.isBlank()) "No repository configured" else "Current: ...${currentRepoUrl.takeLast(20)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showRepoDialog = true }) {
                        Text(if (currentRepoUrl.isBlank()) "Add Repository" else "Change Repository")
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
                onClick = {
                    scope.launch {
                        isResolving = true
                        error = null
                        detectedSource = resolver.resolve(urlInput)
                        isResolving = false
                        if (detectedSource == null) {
                            error = "Could not detect source type. Please check the URL."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = urlInput.isNotBlank() && !isResolving
            ) {
                if (isResolving) {
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

            detectedSource?.let { source ->
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
                            onClick = {
                                scope.launch {
                                    database.sourceDao().upsertTemplates(listOf(source))
                                    database.sourceDao().setUserSource(
                                        UserSourceEntity(domain = source.domain, enabled = true)
                                    )
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to My Sources")
                        }
                    }
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        
        if (showRepoDialog) {
            var tempUrl by remember { mutableStateOf(currentRepoUrl) }
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
                        scope.launch {
                            preferencesManager.setUserRepoUrl(tempUrl)
                            currentRepoUrl = tempUrl
                            showRepoDialog = false
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showRepoDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
