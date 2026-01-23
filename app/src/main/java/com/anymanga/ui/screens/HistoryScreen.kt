package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anymanga.R
import com.anymanga.data.AppDatabase
import com.anymanga.data.HistoryEntity
import com.anymanga.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onMangaClick: (String, String) -> Unit,
    viewModelFactory: com.anymanga.viewmodel.ViewModelFactory
) {
    val viewModel: com.anymanga.viewmodel.HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
    val historyList by viewModel.historyItems.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.history), fontWeight = FontWeight.Bold) },
                actions = {
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllHistory() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            EmptyHistory(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList) { history ->
                    HistoryItem(
                        history = history,
                        onClick = { onMangaClick(history.mangaId, history.sourceId) },
                        onDelete = { viewModel.deleteHistory(history.mangaId) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.History, null, tint = TextGray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.history_empty), color = TextGray, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun HistoryItem(history: HistoryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeString = remember(history.lastReadTime) { sdf.format(Date(history.lastReadTime)) }

    Surface(
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = history.mangaThumbnailUrl,
                contentDescription = history.mangaTitle,
                modifier = Modifier.size(width = 60.dp, height = 85.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(history.mangaTitle, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(history.chapterTitle, color = Primary, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Text(timeString, color = TextGray, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextGray.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}
