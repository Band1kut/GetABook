package com.head2head.getabook.data.scripts

import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptProvider @Inject constructor() {

    /**
     * Генерация универсального скрипта скрытия элементов.
     * Селекторы подставляются динамически.
     */
    private fun generateHideScript(selectors: List<String>): String {
        val cssRules = selectors.joinToString("\n") { "$it { display: none !important; }" }

        return """
            (function() {

                const css = `
                    $cssRules
                `;

                function applyCSS() {
                    try {
                        if (!document.getElementById("getabook-hide-style")) {
                            const style = document.createElement("style");
                            style.id = "getabook-hide-style";
                            style.textContent = css;
                            document.head.appendChild(style);
                            console.log("GetABook: CSS injected");
                        }
                    } catch (e) {
                        console.log("GetABook: error injecting CSS", e);
                    }
                }

                // Первичное внедрение
                applyCSS();

                // Гарантия одного observer
                if (!window.getabookObserverInitialized) {
                    window.getabookObserverInitialized = true;

                    try {
                        const observer = new MutationObserver(() => {
                            applyCSS();
                        });

                        observer.observe(document.documentElement, { childList: true, subtree: true });
                        console.log("GetABook: observer started");
                    } catch (e) {
                        console.log("GetABook: observer error", e);
                    }
                }

            })();
        """.trimIndent()
    }

    /**
     * Публичный метод для применения скрипта.
     * Вызывается и на старте, и на финише.
     */
    fun applyHideScript(selectors: List<String>, webView: WebView) {
        if (selectors.isEmpty()) return
        val script = generateHideScript(selectors)
        webView.evaluateJavascript(script, null)
    }

    /**
     * Тестовый метод для Яндекса.
     * Скрывает .Root.Root_inited
     */
    fun testYandex(webView: WebView) {
        val selectors = listOf(".Root.Root_inited")
        applyHideScript(selectors, webView)
    }
}
