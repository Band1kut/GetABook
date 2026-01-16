package com.head2head.getabook.search

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreen(
    initialUrl: String,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    val viewModel: SearchViewModel = viewModel()

    // Следим за состояниями из ViewModel
    val showDownloadButton by viewModel.showDownloadButton.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isContentReady by viewModel.isContentReady.collectAsState()

    // При первом отображении сбрасываем состояние
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // WebView компонент - ВСЕГДА отображается, но может быть прозрачным
        WebViewComponent(
            initialUrl = initialUrl,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = onWebViewCreated,
            onPageStarted = { url -> viewModel.onPageStarted(url) },
            onPageFinished = { webView, url -> viewModel.onPageFinished(webView, url) }
        )

        // Индикатор загрузки страницы (показываем дольше для Яндекс)
        if (isLoading || !isContentReady) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Кнопка скачивания (только иконка) - только когда контент готов
        if (showDownloadButton && isContentReady) {
            FloatingActionButton(
                onClick = { viewModel.onDownloadClicked() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                content = {
                    Icon(
                        painter = painterResource(android.R.drawable.ic_menu_save),
                        contentDescription = "Скачать аудиокнигу"
                    )
                }
            )
        }
    }
}