package com.anymanga.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anymanga.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Reading Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatsCard("Hours Read", "128", Icons.Default.Timer, Modifier.weight(1f))
                    StatsCard("Chapters", "1,240", Icons.Default.CheckCircle, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
            }
            
            item {
                Text("FAVORITE GENRES", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
                GenreBar("Action", 0.8f, Primary)
                GenreBar("Adventure", 0.6f, Color.Cyan)
                GenreBar("Comedy", 0.3f, Color.Magenta)
            }
            
            item {
                Spacer(Modifier.height(32.dp))
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.BarChart, null, tint = Primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Weekly Goal", fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("You've read 12 chapters this week. 8 more to reach your goal!", color = TextGray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CardDark,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun GenreBar(name: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, color = TextWhite, fontSize = 14.sp)
            Text("${(progress * 100).toInt()}%", color = TextGray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CardDark
        )
    }
}
