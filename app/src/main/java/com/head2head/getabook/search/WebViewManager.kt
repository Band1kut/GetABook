package com.head2head.getabook.search

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
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
        // Настройки (оставить как есть)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // WebViewClient с улучшенной логикой флагов
        webViewClient = object : WebViewClient() {
            // Флаги для каждого домена отдельно
            private val preloadFlags = mutableMapOf<String, Boolean>()

            // Текущий основной URL (для отслеживания навигации)
            private var currentMainUrl: String? = null

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { currentUrl ->
                    // Уведомляем ViewModel
                    onPageStarted(currentUrl)

                    // Определяем базовый домен для флага
                    val domain = extractDomain(currentUrl)

                    // ✅ УМНАЯ ПРЕДЗАГРУЗКА CSS для Яндекса
                    if (currentUrl.contains("yandex.ru", ignoreCase = true)) {
                        // Проверяем по конкретному URL, а не просто "yandex.ru"
                        val isYandexSearch = currentUrl.contains("yandex.ru/search", ignoreCase = true) ||
                                currentUrl.contains("yandex.ru/yandsearch", ignoreCase = true)

                        if (isYandexSearch && preloadFlags[domain] != true) {
                            Log.i("WebViewClient", "Предзагрузка CSS для: $domain")
                            view?.let { webView ->
                                YandexCleanup.injectPreloadCSS(webView, currentUrl)
                                preloadFlags[domain] = true
                            }
                        }
                    }

                    // Обновляем текущий основной URL
                    currentMainUrl = currentUrl
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { currentUrl ->
                    view?.let { webView ->
                        Log.d("WebViewClient", "Страница загружена: ${currentUrl.take(80)}...")

                        // ✅ Основная активация CSS для Яндекса
                        if (currentUrl.contains("yandex.ru", ignoreCase = true)) {
                            val isYandexSearch = currentUrl.contains("yandex.ru/search", ignoreCase = true) ||
                                    currentUrl.contains("yandex.ru/yandsearch", ignoreCase = true)

                            if (isYandexSearch) {
                                // Даём небольшую задержку для гарантии применения CSS
                                webView.postDelayed({
                                    YandexCleanup.injectCleanup(webView, currentUrl)
                                }, 50) // Уменьшили задержку
                            }
                        }

                        // Вызываем колбэк для ViewModel
                        onPageFinished(webView, currentUrl)
                    }
                }
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)

                // Логируем для отладки, но НЕ сбрасываем флаги
                url?.let { currentUrl ->
                    val domain = extractDomain(currentUrl)
                    Log.v("WebViewClient",
                        "История обновлена: domain=$domain, " +
                                "reload=$isReload, " +
                                "preloaded=${preloadFlags[domain] == true}"
                    )

                    // Важно: НЕ СБРАСЫВАЕМ флаги здесь!
                    // Они сбрасываются только при явной перезагрузке или смене домена
                }
            }

            // Вспомогательная функция для извлечения домена
            private fun extractDomain(url: String): String {
                return try {
                    val uri = java.net.URI(url)
                    val host = uri.host ?: "unknown"
                    // Для Яндекса используем полный host, для других - домен
                    if (host.contains("yandex")) host else host.replace("^www\\.".toRegex(), "")
                } catch (e: Exception) {
                    "unknown"
                }
            }
        }

        // Сохраняем ссылку
        onWebViewCreated(this)

        // Загружаем начальный URL
        loadUrl(initialUrl)

        Log.d("WebViewManager", "WebView создан, загружаем: $initialUrl")
    }
}