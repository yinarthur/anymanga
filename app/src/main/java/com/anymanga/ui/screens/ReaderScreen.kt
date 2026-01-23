package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anymanga.data.AppDatabase
import com.anymanga.engine.EngineRegistry
import com.anymanga.model.Page
import com.anymanga.ui.theme.Primary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ReadingMode {
    WEBTOON,
    PAGED_LTR,
    PAGED_RTL
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    chapterUrl: String,
    sourceId: String,
    viewModelFactory: com.anymanga.viewmodel.ViewModelFactory,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val viewModel: com.anymanga.viewmodel.ReaderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsState()
    
    var readingMode by remember { mutableStateOf(ReadingMode.WEBTOON) }
    var showUI by remember { mutableStateOf(true) }
    
    LaunchedEffect(chapterUrl, sourceId) {
        viewModel.loadPages(sourceId, chapterUrl)
    }

    val pages = (state as? com.anymanga.viewmodel.ReaderViewModel.ReaderState.Success)?.pages ?: emptyList()
    val isLoading = state is com.anymanga.viewmodel.ReaderViewModel.ReaderState.Loading
    
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showUI = !showUI }) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Primary)
        } else {
            when (readingMode) {
                ReadingMode.WEBTOON -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(pages) { page ->
                            AsyncImage(model = page.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentScale = ContentScale.FillWidth)
                        }
                    }
                }
                else -> {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), reverseLayout = readingMode == ReadingMode.PAGED_RTL) { pageIndex ->
                        AsyncImage(model = pages[pageIndex].imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    }
                }
            }
        }

        if (showUI && !isLoading) {
            PremiumReaderUI(
                title = "Reader", 
                chapter = "Chapter", 
                currentPage = pagerState.currentPage + 1,
                totalPages = pages.size,
                onBack = onBack,
                onPageChange = { scope.launch { pagerState.scrollToPage(it - 1) } },
                onReadingModeClick = {
                    readingMode = when (readingMode) {
                        ReadingMode.WEBTOON -> ReadingMode.PAGED_LTR
                        ReadingMode.PAGED_LTR -> ReadingMode.PAGED_RTL
                        else -> ReadingMode.WEBTOON
                    }
                },
                readingMode = readingMode
            )
        }
    }
}

@Composable
fun PremiumReaderUI(
    title: String,
    chapter: String,
    currentPage: Int,
    totalPages: Int,
    onBack: () -> Unit,
    onPageChange: (Int) -> Unit,
    onReadingModeClick: () -> Unit,
    readingMode: ReadingMode
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth().height(64.dp), color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(32.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(chapter, color = Primary, fontSize = 12.sp)
                }
            }
        }
        Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth().wrapContentHeight(), color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(onClick = onReadingModeClick) {
                    Text(readingMode.name, color = Color.White)
                }
                if (totalPages > 0 && readingMode != ReadingMode.WEBTOON) {
                    Slider(value = currentPage.toFloat(), onValueChange = { onPageChange(it.toInt()) }, valueRange = 1f..totalPages.toFloat(), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
