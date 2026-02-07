package com.head2head.getabook.presentation.search

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.head2head.getabook.presentation.webview.BookWebViewManager
import com.head2head.getabook.presentation.webview.SearchWebViewManager
import com.head2head.getabook.presentation.webview.WebViewComponent
import com.head2head.getabook.presentation.webview.WebViewCoordinator

@Composable
fun SearchScreen(
    query: String,
    searchManager: SearchWebViewManager,
    bookManager: BookWebViewManager,
    coordinator: WebViewCoordinator,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Coordinator сам подписывается на события WebView и ViewModel
    LaunchedEffect(Unit) {
        Log.d("SearchScreen", "bindToViewModel()")
        coordinator.bindToViewModel(viewModel)
    }

    // Загружаем поисковый URL
    LaunchedEffect(query) {
        val url = viewModel.buildSearchUrl(query)
        searchManager.loadSearchUrl(url)
    }

    // Ошибка загрузки → Toast
    val loadError by viewModel.loadError.collectAsState()
    LaunchedEffect(loadError) {
        if (loadError) {
            Toast.makeText(context, "Не удалось загрузить страницу", Toast.LENGTH_SHORT).show()
            viewModel.clearLoadError()
        }
    }

    // UI состояния
    val isBookMode by viewModel.isBookMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showDownloadButton by viewModel.showDownloadButton.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val pendingBookUrl by viewModel.pendingBookUrl.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        WebViewComponent(
            modifier = Modifier.fillMaxSize(),
            searchManager = searchManager,
            bookManager = bookManager,
            isBookMode = isBookMode,
            pendingBookUrl = pendingBookUrl
        )

        // Оверлей загрузки
        val overlayAlpha by animateFloatAsState(
            targetValue = if (isLoading) 0.6f else 0f,
            animationSpec = tween(200)
        )

        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = overlayAlpha))
                    .pointerInput(Unit) {}
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Кнопка скачивания
        if (showDownloadButton) {
            FloatingActionButton(
                onClick = { viewModel.requestDownload() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
        }

        // Прогресс скачивания
        downloadProgress?.let { progress ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(progress = { progress / 100f })
            }
        }
    }
}
