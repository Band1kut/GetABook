package com.head2head.getabook.search

import android.util.Log
import android.webkit.WebView
import com.head2head.getabook.data.SitesManager

object BookPageChecker {

    fun checkPage(
        webView: WebView,
        url: String,
        onResult: (Boolean, com.head2head.getabook.data.model.AudioBookSite?) -> Unit  // ← ДВА параметра
    ) {
        Log.d("BookPageChecker", "Проверяем страницу: $url")

        // 1. Ищем сайт
        val site = SitesManager.findSite(url)

        if (site == null) {
            Log.d("BookPageChecker", "Сайт не поддерживается")
            onResult(false, null)  // ← Два параметра
            return
        }

        Log.d("BookPageChecker", "Найден сайт: ${site.domain}")

        // 2. Проверяем через JavaScript
        site.isBookPage(webView) { isBookPage ->
            Log.d("BookPageChecker", "Результат проверки: $isBookPage")
            onResult(isBookPage, if (isBookPage) site else null)  // ← Два параметра
        }
    }
}