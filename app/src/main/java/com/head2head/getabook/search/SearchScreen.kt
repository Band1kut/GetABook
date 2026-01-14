package com.head2head.getabook.search

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    initialUrl: String,
    onWebViewCreated: (android.webkit.WebView) -> Unit = {}
) {
    var showDownloadButton by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // WebView компонент
        WebViewComponent(
            initialUrl = initialUrl,
            modifier = Modifier.fillMaxSize(),
            onWebViewCreated = onWebViewCreated,
            onBookPageDetected = { isBookPage ->
                showDownloadButton = isBookPage
                if (isBookPage) {
                    Log.i("SearchScreen", "Показана кнопка скачивания")
                }
            },
            onYandexPage = { webView, url ->  // ← ДОБАВЬТЕ ПАРАМЕТРЫ!
                YandexCleanup.injectCleanup(webView, url)
            }
        )

        // Плавающая кнопка скачивания
        if (showDownloadButton) {
            FloatingActionButton(
                onClick = {
                    Log.d("SearchScreen", "Нажата кнопка скачивания")
                    // TODO: Реализовать скачивание
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)  // align ВНУТРИ Modifier
                    .padding(16.dp),
            ) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_save),
                    contentDescription = "Скачать книгу"
                )
            }
        }
    }
}

// УДАЛИТЕ этот отдельный DownloadButton компонент или исправьте:
/*
@Composable
private fun DownloadButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)  // ОШИБКА: align нельзя использовать вне Box
            .padding(16.dp),
    ) {
        Icon(
            painter = painterResource(android.R.drawable.ic_menu_save),
            contentDescription = "Скачать книгу"
        )
    }
}
*/