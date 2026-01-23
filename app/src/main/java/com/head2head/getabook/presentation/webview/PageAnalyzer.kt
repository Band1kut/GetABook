package com.head2head.getabook.presentation.webview

import android.util.Log
import android.webkit.WebView
import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.data.scripts.ScriptProvider

class PageAnalyzer(
    private val activeSitesRepository: ActiveSitesRepository,
    private val scriptProvider: ScriptProvider,
    private val onBookPageDetected: (Boolean) -> Unit
) {

    fun handleEvent(
        url: String,
        event: PageEvent,
        webView: WebView
    ) {
        val site = activeSitesRepository.getSiteByUrl(url)
        if (site == null) {
            onBookPageDetected(false)
            return
        }


        Log.d("PageAnalyzer", "Найден сайт: ${site.domain}, bookPattern = ${site.bookPattern}")

        // 1. Всегда скрываем элементы активного сайта
        if (site.hideElement.isNotEmpty()) {
            scriptProvider.applyHideScript(site.hideElement, webView)
        }

        // 2. Определяем страницу книги только на Finished
        if (event == PageEvent.Finished) {
            val selector = site.bookPattern
            val js = """
                (function() {
                    return document.querySelector("$selector") != null;
                })();
            """

            webView.evaluateJavascript(js) { result ->
                val isBook = result?.contains("true") == true
                Log.d("PageAnalyzer", "evaluateJavascript = $isBook")
                onBookPageDetected(isBook)
            }
        }
    }
}

