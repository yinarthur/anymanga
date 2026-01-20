package com.anymanga.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anymanga.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsManagerScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Downloads", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Storage Stats
            item {
                Row(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StorageStatCard("App Storage", "4.2 GB", Modifier.weight(1f))
                    StorageStatCard("Total Free", "64.5 GB", Modifier.weight(1f), isPrimary = true)
                }
                
                // Progress Bar
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Device Storage", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("4.2 GB used of 128 GB", color = TextGray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.35f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Primary,
                        trackColor = CardDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("AnyManga occupies 3% of your device", color = TextGray, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
                Spacer(Modifier.height(24.dp))
            }

            // Settings
            item {
                SettingsToggle("Auto-Download", "New chapters download on Wi-Fi", Icons.Default.Wifi, true)
                Spacer(Modifier.height(12.dp))
                SettingsAction("Clear Cache", "Remove temporary files", Icons.Default.CleaningServices) { }
                Spacer(Modifier.height(32.dp))
            }

            // Downloaded Content Header
            item {
                Text("DOWNLOADED CONTENT", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(16.dp))
            }

            // Downloading Item
            item {
                DownloadingItem("Chainsaw Man", "Vol. 12", 45)
                Spacer(Modifier.height(12.dp))
            }

            // Completed Items Mock
            items(listOf("One Piece" to "120 Chapters • 1.2 GB", "Berserk" to "41 Chapters • 850 MB")) { (title, info) ->
                CompletedDownloadItem(title, info)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StorageStatCard(label: String, value: String, modifier: Modifier = Modifier, isPrimary: Boolean = false) {
    Surface(
        modifier = modifier,
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = if (isPrimary) Primary else TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsToggle(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, checked: Boolean) {
    Surface(color = CardDark, shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            Switch(checked = checked, onCheckedChange = { }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary))
        }
    }
}

@Composable
fun SettingsAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(color = CardDark, shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextGray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                Text("Clean", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DownloadingItem(title: String, info: String, progress: Int) {
    Surface(color = Primary.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Box(modifier = Modifier.size(64.dp, 80.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray)) {
                Icon(Icons.Default.Downloading, null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.Close, null, tint = TextGray, modifier = Modifier.size(16.dp))
                }
                Text(info, color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$progress% Complete", color = Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("2.4 MB/s", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Primary,
                    trackColor = CardDark
                )
            }
        }
    }
}

@Composable
fun CompletedDownloadItem(title: String, info: String) {
    Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp, 80.dp).clip(RoundedCornerShape(8.dp)).background(Color.Gray))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Icon(Icons.Default.MoreVert, null, tint = TextGray)
                }
                Text(info, color = TextGray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("DOWNLOADED", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
