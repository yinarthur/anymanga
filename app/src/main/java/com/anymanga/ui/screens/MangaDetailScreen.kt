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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.anymanga.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDetailScreen(
    mangaUrl: String,
    sourceId: String,
    viewModelFactory: com.anymanga.viewmodel.ViewModelFactory,
    onBack: () -> Unit,
    onRead: (Chapter) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    
    val viewModel: com.anymanga.viewmodel.MangaDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsState()
    
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(mangaUrl, sourceId) {
        viewModel.loadDetails(sourceId, mangaUrl)
    }

    val recordHistory = { manga: Manga, chapter: Chapter ->
        scope.launch {
            database.historyDao().insertHistory(
                HistoryEntity(
                    mangaId = mangaUrl,
                    sourceId = sourceId,
                    mangaTitle = manga.title,
                    mangaThumbnailUrl = manga.thumbnailUrl ?: "",
                    chapterId = chapter.url,
                    chapterTitle = chapter.name
                )
            )
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (val detailState = state) {
            is com.anymanga.viewmodel.MangaDetailViewModel.DetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is com.anymanga.viewmodel.MangaDetailViewModel.DetailState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    item {
                        MangaHeader(manga = detailState.manga)
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (detailState.chapters.isNotEmpty()) {
                                        val firstChapter = detailState.chapters.first()
                                        recordHistory(detailState.manga, firstChapter)
                                        onRead(firstChapter)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.read_now), fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { isFavorite = !isFavorite },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                    detailState.manga.description?.let { desc ->
                        item {
                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                Text(
                                    text = stringResource(R.string.synopsis),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = desc,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.chapters_count, detailState.chapters.size),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    items(detailState.chapters) { chapter ->
                        ChapterItem(chapter = chapter, onClick = { 
                            recordHistory(detailState.manga, chapter)
                            onRead(chapter) 
                        })
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
            is com.anymanga.viewmodel.MangaDetailViewModel.DetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = detailState.message, color = MaterialTheme.colorScheme.error)
                }
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
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = manga?.title ?: stringResource(R.string.unknown),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            manga?.author?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = manga?.status ?: stringResource(R.string.unknown),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChapterItem(chapter: Chapter, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
