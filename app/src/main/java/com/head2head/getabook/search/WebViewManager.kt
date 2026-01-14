package com.head2head.getabook.search

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun WebViewComponent(
    initialUrl: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {},
    onBookPageDetected: (Boolean) -> Unit = {},  // ← Из Boolean в Boolean
    onYandexPage: (WebView, String) -> Unit = {}  // ← Из (WebView, String) в Unit
) {
    AndroidView(
        factory = { context ->
            createWebView(
                context = context,
                initialUrl = initialUrl,
                onWebViewCreated = onWebViewCreated,
                onBookPageDetected = onBookPageDetected,
                onYandexPage = onYandexPage
            )
        },
        modifier = modifier
    )
}

private fun createWebView(
    context: Context,
    initialUrl: String,
    onWebViewCreated: (WebView) -> Unit,
    onBookPageDetected: (Boolean) -> Unit,  // ← Тип совпадает
    onYandexPage: (WebView, String) -> Unit  // ← Тип совпадает
): WebView {
    return WebView(context).apply {
        // Настройки
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        // WebViewClient с логикой
        webViewClient = createWebViewClient(
            onYandexPage = onYandexPage,  // ← Просто передаём как есть
            onBookPageDetected = onBookPageDetected
        )

        // Сохраняем ссылку
        onWebViewCreated(this)

        // Загружаем начальный URL
        loadUrl(initialUrl)

        Log.d("WebViewManager", "WebView создан, загружаем: $initialUrl")
    }
}

private fun createWebViewClient(
    onYandexPage: (WebView, String) -> Unit,  // ← Тип совпадает
    onBookPageDetected: (Boolean) -> Unit  // ← Тип совпадает
): WebViewClient {
    return object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let {
                if (it.contains("yandex.ru", ignoreCase = true)) {
                    Log.d("WebViewManager", "Начинается загрузка Яндекс")
                }
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let { currentUrl ->
                Log.d("WebViewManager", "Страница загружена: $currentUrl")

                view?.let { webView ->
                    // Яндекс
                    if (currentUrl.contains("yandex.ru", ignoreCase = true)) {
                        onYandexPage(webView, currentUrl)  // ← Правильный вызов
                    }

                    // Проверка книг
                    BookPageChecker.checkPage(webView, currentUrl) { isBookPage, site ->
                        onBookPageDetected(isBookPage)  // ← Правильный вызов
                    }
                }
            }
        }
    }
}