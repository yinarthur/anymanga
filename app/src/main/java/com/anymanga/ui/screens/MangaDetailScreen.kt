package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anymanga.data.AppDatabase
import com.anymanga.data.HistoryEntity
import com.anymanga.engine.EngineRegistry
import com.anymanga.model.Chapter
import com.anymanga.model.Manga
import com.anymanga.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.anymanga.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(
    mangaUrl: String,
    sourceId: String,
    onBack: () -> Unit,
    onRead: (Chapter) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    
    var manga by remember { mutableStateOf<Manga?>(null) }
    var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(mangaUrl, sourceId) {
        isLoading = true
        try {
            val template = database.sourceDao().getAllTemplates().first().find { it.id == sourceId }
            if (template != null) {
                val engine = EngineRegistry.getEngineForTemplate(template)
                manga = engine.getMangaDetails(template.baseUrl, mangaUrl)
                chapters = engine.getChapters(template.baseUrl, mangaUrl)
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    val recordHistory = { chapter: Chapter ->
        scope.launch {
            manga?.let {
                database.historyDao().insertHistory(
                    HistoryEntity(
                        mangaId = mangaUrl,
                        sourceId = sourceId,
                        mangaTitle = it.title,
                        mangaThumbnailUrl = it.thumbnailUrl ?: "",
                        chapterId = chapter.url,
                        chapterTitle = chapter.name
                    )
                )
            }
        }
    }
    
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    MangaHeader(manga = manga)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (chapters.isNotEmpty()) {
                                    val firstChapter = chapters.first()
                                    recordHistory(firstChapter)
                                    onRead(firstChapter)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.read_now), fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { isFavorite = !isFavorite }, modifier = Modifier.size(48.dp).background(CardDark, RoundedCornerShape(12.dp))) {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorite", tint = if (isFavorite) Color.Red else Color.White)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
                manga?.description?.let { desc ->
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(stringResource(R.string.synopsis), color = TextWhite, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(desc, color = TextGray, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
                item {
                    Text("Chapters (${chapters.size})", color = TextWhite, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(12.dp))
                }
                items(chapters) { chapter ->
                    ChapterItem(chapter = chapter, onClick = { recordHistory(chapter); onRead(chapter) })
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun MangaHeader(manga: Manga?) {
    Row(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        AsyncImage(
            model = manga?.thumbnailUrl ?: "",
            contentDescription = manga?.title,
            modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(16.dp)).background(CardDark),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(manga?.title ?: "Unknown", color = TextWhite, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            manga?.author?.let { Text(it, color = TextGray, style = MaterialTheme.typography.bodyMedium) }
            Spacer(Modifier.height(8.dp))
            Surface(color = Primary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Text(manga?.status ?: "Unknown", color = Primary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 6.dp),
        color = CardDark, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(chapter.name, color = TextWhite, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.Download, "Download", tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}
