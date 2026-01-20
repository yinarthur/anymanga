package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anymanga.R
import com.anymanga.data.AppDatabase
import com.anymanga.data.SourceTemplateEntity
import com.anymanga.engine.EngineRegistry
import com.anymanga.model.Manga
import com.anymanga.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onMangaClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    
    var updates by remember { mutableStateOf<List<Pair<SourceTemplateEntity, List<Manga>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val loadUpdates = suspend {
        val enabledSources = database.sourceDao().observeEnabledSources().first()
        val allUpdates = mutableListOf<Pair<SourceTemplateEntity, List<Manga>>>()
        
        enabledSources.forEach { source ->
            try {
                val engine = EngineRegistry.getEngineForTemplate(source)
                val latest = engine.getLatestManga(source.baseUrl, 1)
                if (latest.isNotEmpty()) {
                    allUpdates.add(source to latest)
                }
            } catch (e: Exception) {
                // Skip failed sources
            }
        }
        updates = allUpdates
    }

    LaunchedEffect(Unit) {
        isLoading = true
        loadUpdates()
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Good Morning,", color = TextGray, style = MaterialTheme.typography.bodyLarge)
                Text("Reader", color = TextWhite, style = MaterialTheme.typography.headlineLarge)
            }
        }
    ) { padding ->
        // Note: PullRefresh should be implemented here in a production app.
        // For now, using a simple LazyColumn update flow.
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
            } else if (updates.isEmpty()) {
                EmptyHome()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    updates.forEach { (source, mangaList) ->
                        item {
                            SectionHeader("${stringResource(R.string.updates)}: ${source.name}", Icons.Default.Star)
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(mangaList) { manga ->
                                    MangaCard(
                                        manga = manga,
                                        onClick = { onMangaClick(manga.url, source.id) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHome() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Default.Extension, null, tint = TextGray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("No Updates Found", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enable sources in the Browse tab to see their latest updates here",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MangaCard(manga: Manga, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(140.dp).clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = manga.thumbnailUrl,
            contentDescription = manga.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(manga.title, color = TextWhite, style = MaterialTheme.typography.titleMedium, maxLines = 2)
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = TextWhite, style = MaterialTheme.typography.titleLarge)
    }
}
