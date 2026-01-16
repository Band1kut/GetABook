package com.head2head.getabook.search

import android.util.Log
import android.webkit.WebView

object YandexCleanup {
    private const val TAG = "YandexCleanup"

    // Флаг глобальной инъекции для всего приложения
    private var globalCSSInjected = false

    /**
     * Предзагрузка CSS при начале загрузки страницы
     */
    fun injectPreloadCSS(webView: WebView, url: String?) {
        url?.takeIf {
            it.contains("yandex.ru/search", ignoreCase = true) ||
                    it.contains("yandex.ru/yandsearch", ignoreCase = true)
        }?.let {
            Log.d(TAG, "ПРЕДЗАГРУЗКА CSS для Яндекса")

            // Инъекция в onPageStarted - РАНЬШЕ чем страница отобразится
            val preloadScript = """
                (function() {
                    // ИНЪЕКЦИЯ НА САМОМ РАННЕМ ЭТАПЕ
                    
                    // 1. Немедленно скрываем body чтобы избежать мерцания
                    if (document.body) {
                        document.body.style.opacity = '0';
                        document.body.style.transition = 'opacity 0.3s ease';
                        document.body.style.visibility = 'hidden';
                    }
                    
                    // 2. Создаём и внедряем стиль заранее
                    var style = document.createElement('style');
                    style.id = 'yandex-preload-css';
                    style.textContent = `
                        /* Скрываем все потенциальные Яндекс-блоки */
                        div.Root, 
                        div[class*="Root"], 
                        div.layout__container,
                        .Root_inited,
                        .header2,
                        .navigation,
                        .search-arrow {
                            display: none !important;
                            visibility: hidden !important;
                            height: 0 !important;
                            opacity: 0 !important;
                            overflow: hidden !important;
                        }
                        
                        /* Результаты поиска - нормальные отступы */
                        .serp-list,
                        .serp-item {
                            margin-top: 0 !important;
                            padding-top: 0 !important;
                        }
                        
                        /* Body без лишних отступов */
                        body {
                            padding-top: 0 !important;
                            margin-top: 0 !important;
                            overflow-x: hidden !important;
                        }
                    `;
                    
                    // 3. Внедряем максимально рано
                    if (document.head) {
                        document.head.appendChild(style);
                    } else if (document.documentElement) {
                        document.documentElement.insertBefore(style, document.documentElement.firstChild);
                    }
                    
                    // 4. Функция для плавного показа
                    window.getabookShowContent = function() {
                        if (document.body) {
                            // Плавно показываем контент
                            setTimeout(function() {
                                document.body.style.opacity = '1';
                                document.body.style.visibility = 'visible';
                            }, 50);
                        }
                        console.log('GetABook: Яндекс готов к отображению');
                    };
                    
                    // 5. Запускаем показ при готовности
                    if (document.readyState === 'complete' || document.readyState === 'interactive') {
                        setTimeout(window.getabookShowContent, 100);
                    } else {
                        document.addEventListener('DOMContentLoaded', function() {
                            setTimeout(window.getabookShowContent, 100);
                        });
                    }
                    
                    return 'preload_injected';
                })();
            """.trimIndent()

            webView.evaluateJavascript(preloadScript) { result ->
                Log.d(TAG, "Предзагрузка CSS выполнена: $result")
            }
        }
    }

    /**
     * Основная функция для инъекции CSS в Яндекс
     */
    fun injectCleanup(webView: WebView, url: String?) {
        Log.d(TAG, "Запуск очистки для URL: ${url?.take(80)}...")

        url?.let {
            val isYandexSearch = it.contains("yandex.ru/search", ignoreCase = true) ||
                    it.contains("yandex.ru/yandsearch", ignoreCase = true)

            if (isYandexSearch) {
                // Если уже есть предзагрузка, просто активируем показ
                val activateScript = """
                    (function() {
                        // Если есть функция показа - вызываем её
                        if (typeof window.getabookShowContent === 'function') {
                            window.getabookShowContent();
                            return 'activated_via_preload';
                        }
                        
                        // Иначе стандартная инъекция
                        return injectStandardCSS();
                    })();
                    
                    function injectStandardCSS() {
                        if (window.getabookCSSInjected) {
                            return 'already_injected';
                        }
                        
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
                                opacity: 1 !important;
                                visibility: visible !important;
                            }
                        `;
                        
                        if (document.head) {
                            document.head.appendChild(style);
                            window.getabookCSSInjected = true;
                            return 'css_injected';
                        } else {
                            document.body.appendChild(style);
                            window.getabookCSSInjected = true;
                            return 'css_injected_to_body';
                        }
                    }
                """.trimIndent()

                webView.evaluateJavascript(activateScript) { result ->
                    Log.d(TAG, "CSS активация выполнена: $result")
                }
            }
        }
    }
}