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
    onPageStarted: (String) -> Unit = {},
    onPageFinished: (WebView, String) -> Unit = { _, _ -> }
) {
    AndroidView(
        factory = { context ->
            createWebView(
                context = context,
                initialUrl = initialUrl,
                onWebViewCreated = onWebViewCreated,
                onPageStarted = onPageStarted,
                onPageFinished = onPageFinished
            )
        },
        modifier = modifier
    )
}

private fun createWebView(
    context: Context,
    initialUrl: String,
    onWebViewCreated: (WebView) -> Unit,
    onPageStarted: (String) -> Unit,
    onPageFinished: (WebView, String) -> Unit
): WebView {
    return WebView(context).apply {
        // Настройки
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        // WebViewClient с оптимизированной логикой
        webViewClient = createWebViewClient(
            onPageStarted = onPageStarted,
            onPageFinished = onPageFinished
        )

        // Сохраняем ссылку
        onWebViewCreated(this)

        // Загружаем начальный URL
        loadUrl(initialUrl)

        Log.d("WebViewManager", "WebView создан, загружаем: $initialUrl")
    }
}

private fun createWebViewClient(
    onPageStarted: (String) -> Unit,
    onPageFinished: (WebView, String) -> Unit
): WebViewClient {
    // Флаг для отслеживания выполнения CSS инъекции
    var isYandexCleanupInjected = false

    return object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            url?.let { currentUrl ->
                // ТОЛЬКО колбэк, никаких CSS инъекций!
                onPageStarted(currentUrl)

                // Сбрасываем флаг при начале новой загрузки
                if (!currentUrl.contains("yandex.ru", ignoreCase = true)) {
                    isYandexCleanupInjected = false
                    Log.v("WebView", "Сброс флага CSS инъекции (не Яндекс)")
                }
            }
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            url?.let { currentUrl ->
                view?.let { webView ->
                    Log.d("WebView", "Страница загружена: ${currentUrl.take(80)}...")

                    // Яндекс очистка - ТОЛЬКО ЗДЕСЬ и ТОЛЬКО ОДИН РАЗ
                    if (currentUrl.contains("yandex.ru", ignoreCase = true)) {
                        if (!isYandexCleanupInjected) {
                            Log.i("WebView", "Первая загрузка Яндекс, инжектим CSS")
                            YandexCleanup.injectCleanup(webView, currentUrl)
                            isYandexCleanupInjected = true
                        } else {
                            Log.d("WebView", "CSS уже инжектирован, пропускаем (возврат из кэша)")
                        }
                    } else {
                        // Если это не Яндекс, сбрасываем флаг
                        isYandexCleanupInjected = false
                    }

                    // Вызываем колбэк
                    onPageFinished(webView, currentUrl)
                }
            }
        }
    }
}