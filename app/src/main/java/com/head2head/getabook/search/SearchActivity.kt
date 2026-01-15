package com.head2head.getabook.search

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.head2head.getabook.data.SitesManager
import com.head2head.getabook.ui.theme.GetABookTheme
import android.util.Log

class SearchActivity : ComponentActivity() {
    private val TAG = "SearchActivity"
    var webView: WebView? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "SearchActivity создан")

        // Получаем сырой запрос из intent
        val rawQuery = intent.getStringExtra("RAW_QUERY") ?: ""
        Log.d(TAG, "Получен запрос из intent: '${rawQuery.take(50)}...' (${rawQuery.length} символов)")

        // Создаём финальный URL для поиска через SitesManager
        val startTime = System.currentTimeMillis()
        val finalUrl = SitesManager.buildSearchUrl(rawQuery)
        val buildTime = System.currentTimeMillis() - startTime

        Log.i(TAG, "URL построен за ${buildTime}ms")
        Log.d(TAG, "Финальный URL (первые 100 символов): ${finalUrl.take(100)}...")

        setContent {
            GetABookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchScreen(
                        initialUrl = finalUrl,
                        onWebViewCreated = {
                            webView = it
                            Log.d(TAG, "WebView создан и сохранён")
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Нажата кнопка назад")

        if (webView?.canGoBack() == true) {
            Log.d(TAG, "WebView может вернуться назад, выполняем goBack()")
            webView?.goBack()
        } else {
            Log.d(TAG, "WebView не может вернуться, закрываем Activity")
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "SearchActivity уничтожен")
    }
}