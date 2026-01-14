package com.head2head.getabook.search

import android.util.Log
import android.webkit.WebView

object YandexCleanup {

    fun injectCleanup(webView: WebView, url: String?) {
        Log.d("YandexCleanup", "Проверяем URL: $url")

        url?.let {
            val isYandexSearch = it.contains("yandex.ru/search", ignoreCase = true) ||
                    it.contains("yandex.ru/yandsearch", ignoreCase = true)

            if (isYandexSearch) {
                Log.d("YandexCleanup", "Запускаем очистку Яндекс")
                injectCSS(webView)
            }
        }
    }

    private fun injectCSS(webView: WebView) {
        val cssInjection = """
            (function() {
                console.log('GetABook: Внедряем CSS для скрытия Root');
                
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = `
                    div.Root.Root_inited {
                        display: none !important;
                        visibility: hidden !important;
                        height: 0 !important;
                        opacity: 0 !important;
                        overflow: hidden !important;
                    }
                    
                    div[class*="Root"] {
                        display: none !important;
                    }
                    
                    div.layout__container {
                        display: none !important;
                    }
                    
                    .serp-list {
                        margin-top: 0 !important;
                        padding-top: 0 !important;
                    }
                    
                    body {
                        padding-top: 0 !important;
                        margin-top: 0 !important;
                        overflow-x: hidden !important;
                    }
                `;
                
                if (document.head) {
                    document.head.appendChild(style);
                    return 'css_injected';
                } else {
                    document.body.appendChild(style);
                    return 'css_injected_to_body';
                }
            })()
        """.trimIndent()

        webView.evaluateJavascript(cssInjection) { result ->
            Log.d("YandexCleanup", "CSS инъекция выполнена: $result")
        }
    }
}