package com.head2head.getabook.data.scripts

import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptProvider @Inject constructor() {

    /**
     * 1) CLICK HOOK — ВСЕГДА
     * Сообщает Android о намерении навигации.
     * НЕ отменяет переход.
     * НЕ вызывает notify().
     */
    private val clickHookJs = """
        (function() {
            document.addEventListener('click', function(e) {
                let el = e.target;
                while (el && el.tagName !== 'A') el = el.parentElement;
                if (!el || !el.href) return;

                try { window.Android.onUserIntentNavigate(); } catch(e) {}
            }, true);
        })();
    """.trimIndent()

    /**
     * 2) SPA HOOK — ТОЛЬКО ДЛЯ НЕ‑ПОИСКОВИКОВ
     * Перехватывает pushState/replaceState/popstate/location.assign/replace.
     * Сообщает Android о SPA‑переходе.
     */
    private val spaHookJs = """
        (function() {
            function notify(url) {
                try { window.Android.onSpaNavigation(url); } catch(e) {}
            }

            const pushState = history.pushState;
            history.pushState = function(state, title, url) {
                notify(url);
                return pushState.apply(this, arguments);
            };

            const replaceState = history.replaceState;
            history.replaceState = function(state, title, url) {
                notify(url);
                return replaceState.apply(this, arguments);
            };

            window.addEventListener('popstate', function() {
                notify(location.href);
            });

            const assign = window.location.assign;
            window.location.assign = function(url) {
                notify(url);
                return assign.call(window.location, url);
            };

            const replace = window.location.replace;
            window.location.replace = function(url) {
                notify(url);
                return replace.call(window.location, url);
            };
        })();
    """.trimIndent()

    fun injectClickHook(webView: WebView) {
        webView.evaluateJavascript(clickHookJs, null)
    }

    fun injectSpaHook(webView: WebView) {
        webView.evaluateJavascript(spaHookJs, null)
    }

    fun applyHideScript(selectors: List<String>, webView: WebView) {
        if (selectors.isEmpty()) return

        val cssRules = selectors.joinToString("\n") { "$it { display: none !important; }" }

        val script = """
            (function() {
                const css = `$cssRules`;

                function applyCSS() {
                    if (!document.getElementById("getabook-hide-style")) {
                        const style = document.createElement("style");
                        style.id = "getabook-hide-style";
                        style.textContent = css;
                        document.head.appendChild(style);
                    }
                }

                applyCSS();

                if (!window.getabookObserverInitialized) {
                    window.getabookObserverInitialized = true;
                    const observer = new MutationObserver(applyCSS);
                    observer.observe(document.documentElement, { childList: true, subtree: true });
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }
}
