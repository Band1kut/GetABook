package com.head2head.getabook.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.head2head.getabook.data.settings.SearchEngine

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        viewModel.setDownloadFolder(uri?.toString())
    }

    val listState = rememberLazyListState()

    // Показывать ли нижнюю тень
    val showBottomShadow by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible < (listState.layoutInfo.totalItemsCount - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ---------------------------
            // 0. Верхняя панель
            // ---------------------------
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                    Text(
                        "Настройки",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }


            // ---------------------------
            // 1. Поисковик
            // ---------------------------
            item {
                Text("Поисковик", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SearchEngine.entries.forEach { engine ->
                        FilterChip(
                            selected = state.searchEngine == engine,
                            onClick = { viewModel.setSearchEngine(engine) },
                            label = { Text(engine.displayName) }
                        )
                    }
                }
            }

            item { Divider() }

            // ---------------------------
            // 2. Папка для сохранения
            // ---------------------------
            item {
                Text("Папка для сохранения", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Text(state.downloadFolder ?: "Не выбрана")

                Spacer(Modifier.height(8.dp))

                Button(onClick = { folderPicker.launch(null) }) {
                    Text("Выбрать папку")
                }
            }

            item { Divider() }

            // ---------------------------
            // 3. Сайты — кнопки
            // ---------------------------
            item {
                Text("Сайты", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row {
                    Button(onClick = { viewModel.selectAllSites() }) {
                        Text("Выделить все")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.deselectAllSites() }) {
                        Text("Снять выделение")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // ---------------------------
            // 4. Сайты — сетка 2 столбца
            // ---------------------------
            items(state.allSites.chunked(2)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { domain ->
                        val checked = domain in state.enabledSites

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    val newSet = if (isChecked) {
                                        state.enabledSites + domain
                                    } else {
                                        state.enabledSites - domain
                                    }
                                    viewModel.setEnabledSites(newSet)
                                }
                            )
                            Text(domain)
                        }
                    }

                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // ---------------------------
        // Нижняя тень
        // ---------------------------
        if (showBottomShadow) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.30f)
                            )
                        )
                    )
            )
        }

    }
}

private val SearchEngine.displayName: String
    get() = when (this) {
        SearchEngine.YANDEX -> "Яндекс"
        SearchEngine.GOOGLE -> "Google"
    }
