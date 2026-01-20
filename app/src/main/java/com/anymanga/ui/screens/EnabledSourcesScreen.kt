package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anymanga.data.AppDatabase
import com.anymanga.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnabledSourcesScreen(onSourceClick: (String) -> Unit) {
    val context = LocalContext.current
    val database = remember {
        androidx.room.Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "anymanga.db"
        ).build()
    }
    val sourceDao = database.sourceDao()
    
    val enabledSources by sourceDao.observeEnabledSources().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        CenterAlignedTopAppBar(
            title = { Text(text = "Enabled Sources", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDark)
        )

        if (enabledSources.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(text = "No sources enabled", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(enabledSources) { source ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSourceClick(source.id) },
                        color = CardDark,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = source.name, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = source.lang.uppercase(), color = TextGray, fontSize = 12.sp)
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}
