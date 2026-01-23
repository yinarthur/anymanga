package com.anymanga.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anymanga.data.*
import com.anymanga.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesCatalogScreen(
    navController: androidx.navigation.NavController,
    viewModelFactory: com.anymanga.viewmodel.ViewModelFactory,
    onAddSourceClick: () -> Unit = {}
) {
    val viewModel: com.anymanga.viewmodel.SourceCatalogViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLang by viewModel.selectedLang.collectAsState()
    val filteredSources by viewModel.sources.collectAsState()
    val languages by viewModel.languages.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Sources Catalog", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSourceClick,
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Custom Source")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Search sources...", color = TextGray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextGray) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    )
                )
                
                Spacer(Modifier.height(8.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(languages) { lang ->
                        val isSelected = selectedLang == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedLang(lang) },
                            label = { Text(text = lang.uppercase()) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = TextGray,
                                selectedLabelColor = Color.White,
                                selectedContainerColor = Primary,
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.White.copy(alpha = 0.1f),
                                selectedBorderColor = Primary
                            )
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredSources, key = { it.template.id }) { sourceWithStatus ->
                    SourceItem(
                        source = sourceWithStatus.template,
                        isEnabled = sourceWithStatus.userSource?.enabled == true,
                        onToggle = { enabled ->
                            viewModel.toggleSource(sourceWithStatus.template.domain, enabled)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceItem(
    source: SourceTemplateEntity,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        color = CardDark,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = source.name, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "${source.lang.uppercase()} • ${source.engineType}", color = TextGray, fontSize = 12.sp)
                Text(text = source.domain, color = Primary, fontSize = 11.sp)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Primary,
                    checkedTrackColor = Primary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
