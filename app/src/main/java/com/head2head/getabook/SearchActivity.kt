package com.head2head.getabook

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.head2head.getabook.data.SitesManager
import com.head2head.getabook.ui.theme.GetABookTheme
import android.util.Log

class SearchActivity : ComponentActivity() {
    // Делаем public для доступа из Compose
    var webView: WebView? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetABookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchScreen(
                        initialUrl = intent.getStringExtra("SEARCH_URL") ?: "https://yandex.ru",
                        onWebViewCreated = { webView = it }
                    )
                }
            }
        }
    }

    // Функция для очистки Яндекс
    // Заменяем старый метод на этот:
    fun injectYandexCleanup(webView: WebView, url: String?) {
        Log.d("YandexCleanup", "Проверяем URL: $url")

        url?.let {
            val isYandexSearch = it.contains("yandex.ru/search", ignoreCase = true) ||
                    it.contains("yandex.ru/yandsearch", ignoreCase = true)

            Log.d("YandexCleanup", "Это Яндекс поиск: $isYandexSearch")

            if (isYandexSearch) {
                Log.d("YandexCleanup", "Запускаем мгновенную очистку через CSS")

                // CSS инъекция - работает мгновенно
                val cssInjection = """
                (function() {
                    console.log('GetABook: Внедряем CSS для скрытия Root');
                    
                    // Создаём стиль
                    var style = document.createElement('style');
                    style.type = 'text/css';
                    style.innerHTML = `
                        /* Скрываем Root блок */
                        div.Root.Root_inited {
                            display: none !important;
                            visibility: hidden !important;
                            height: 0 !important;
                            opacity: 0 !important;
                            overflow: hidden !important;
                        }
                        
                        /* Альтернативные селекторы */
                        div[class*="Root"] {
                            display: none !important;
                        }
                        
                        div.layout__container {
                            display: none !important;
                        }
                        
                        /* Делаем результаты поиска видимыми */
                        .serp-list {
                            margin-top: 0 !important;
                            padding-top: 0 !important;
                        }
                        
                        /* Убираем отступы body */
                        body {
                            padding-top: 0 !important;
                            margin-top: 0 !important;
                            overflow-x: hidden !important;
                        }
                    `;
                    
                    // Добавляем в head
                    if (document.head) {
                        document.head.appendChild(style);
                        console.log('GetABook: CSS добавлен в head');
                        return 'css_injected';
                    } else {
                        // Если head ещё не загружен, добавляем к body
                        document.body.appendChild(style);
                        console.log('GetABook: CSS добавлен к body');
                        return 'css_injected_to_body';
                    }
                })()
            """.trimIndent()

                // Запускаем немедленно
                webView.evaluateJavascript(cssInjection) { result ->
                    Log.d("YandexCleanup", "CSS инъекция выполнена: $result")
                }


            }
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

@Composable
fun SearchScreen(
    initialUrl: String,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    var showDownloadButton by remember { mutableStateOf(false) }
    var currentSite by remember { mutableStateOf<com.head2head.getabook.data.model.AudioBookSite?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    // Сохраняем WebView для доступа из Activity
                    onWebViewCreated(this)

                    settings.javaScriptEnabled = true

                    // ИНТЕГРАЦИЯ В WebViewClient - вот здесь!
                    webViewClient = object : WebViewClient() {

                        // Этот метод вызывается при начале загрузки страницы
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)

                            url?.let { currentUrl ->
                                if (currentUrl.contains("yandex.ru", ignoreCase = true)) {
                                    Log.d("YandexCleanup", "Начинаем загрузку Яндекс, готовим инъекцию")
                                }
                            }
                        }


                        override fun onPageFinished(view: WebView?, url: String?) {
                            url?.let { currentUrl ->
                                Log.d("WebView", "Загружена страница: $currentUrl")

                                // 1. Очистка Яндекс (если это Яндекс)
                                if (currentUrl.contains("yandex.ru", ignoreCase = true)) {
                                    // Вызываем функцию очистки через Activity
                                    (context as? SearchActivity)?.injectYandexCleanup(this@apply, currentUrl)
                                }

                                // 2. Ищем сайт по URL
                                val site = SitesManager.findSite(currentUrl)

                                if (site != null) {
                                    Log.d("WebView", "Найден поддерживаемый сайт: ${site.domain}")

                                    // 3. Проверяем через JavaScript, страница ли это книги
                                    site.isBookPage(this@apply) { isBookPage ->
                                        showDownloadButton = isBookPage
                                        currentSite = if (isBookPage) site else null

                                        Log.d(
                                            "SearchActivity",
                                            "URL: $currentUrl\n" +
                                                    "Сайт: ${site.domain}\n" +
                                                    "Книга: $isBookPage"
                                        )

                                        if (isBookPage) {
                                            Log.i("SearchActivity", "Показана кнопка скачивания")
                                        }
                                    }
                                } else {
                                    // Сайт не поддерживается
                                    Log.d("WebView", "Сайт не поддерживается: $currentUrl")
                                    showDownloadButton = false
                                    currentSite = null
                                }
                            }
                        }
                    }

                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    loadUrl(initialUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Плавающая кнопка скачивания
        if (showDownloadButton) {
            FloatingActionButton(
                onClick = {
                    Log.d("Download", "Нажата кнопка скачивания")
                    // TODO: Реализовать скачивание
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
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