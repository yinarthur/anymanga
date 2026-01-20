package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LibraryBooks
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
import com.anymanga.engine.EngineRegistry
import com.anymanga.model.Manga
import com.anymanga.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onMangaClick: (String, String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, Manga>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    
    // Perform search across all enabled sources
    val performSearch: () -> Unit = {
        if (searchQuery.isNotEmpty()) {
            scope.launch {
                isSearching = true
                try {
                    val enabledSources = database.sourceDao().observeEnabledSources().first()
                    val allResults = mutableListOf<Pair<String, Manga>>()
                    
                    enabledSources.forEach { source ->
                        try {
                            val engine = EngineRegistry.getEngineForTemplate(source)
                            val results = engine.searchManga(source.baseUrl, searchQuery, 1)
                            allResults.addAll(results.map { source.id to it })
                        } catch (e: Exception) {
                            // Skip failed sources
                        }
                    }
                    searchResults = allResults
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    isSearching = false
                }
            }
        }
    }
    
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search manga...", color = TextGray) },
                    trailingIcon = {
                        TextButton(
                            onClick = performSearch,
                            enabled = searchQuery.isNotEmpty() && !isSearching
                        ) {
                            Text("Search", color = if (searchQuery.isNotEmpty()) Primary else TextGray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            when {
                isSearching -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                searchResults.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(searchResults) { (sourceId, manga) ->
                            MangaGridItem(
                                manga = manga,
                                onClick = { onMangaClick(manga.url, sourceId) }
                            )
                        }
                    }
                }
                else -> {
                    EmptyLibrary()
                }
            }
        }
    }
}

@Composable
fun EmptyLibrary() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Default.LibraryBooks, null, tint = TextGray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Your Library is Empty", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Search for manga to add to your library",
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MangaGridItem(manga: Manga, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        AsyncImage(
            model = manga.thumbnailUrl,
            contentDescription = manga.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            manga.title,
            color = TextWhite,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
