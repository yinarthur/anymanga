package com.anymanga.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anymanga.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val settings = uiState.settings
    val diagnosticsText = buildString {
        append("AnyManga Diagnostics\n")
        append("====================\n")
        append("App Version: 1.0.0\n")
        append("Templates Count: ${uiState.templatesCount}\n")
        append("Repository URL: ${settings?.repositoryUrl ?: "None"}\n")
        append("Last Sync: ${settings?.lastSyncEpoch?.let { if (it > 0) sdf.format(Date(it)) else "Never" }}\n")
        append("Last ETag: ${settings?.lastEtag ?: "None"}\n")
        append("Last Error: ${settings?.lastError ?: "None"}\n")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(diagnosticsText))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy to Clipboard")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = diagnosticsText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.syncNow() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSyncing
                ) {
                    Text(if (uiState.isSyncing) "Syncing..." else "Run Sync Now")
                }
            }
        }
    }
}
