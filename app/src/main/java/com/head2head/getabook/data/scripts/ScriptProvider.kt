package com.head2head.getabook.data.scripts

import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptProvider @Inject constructor() {

    /**
     * CLICK HOOK — для поисковиков
     * Сообщает Android о намерении навигации.
     * НЕ отменяет переход.
     */
    private val searchClickHookJs = """
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
     * CLICK HOOK — для книжных сайтов
     * Перехватывает клик, берёт href, отменяет действие страницы,
     * передаёт URL в Android → loadUrl().
     */
    private val bookClickHookJs = """
        (function() {
            document.addEventListener('click', function(e) {
                let el = e.target;
                while (el && el.tagName !== 'A') el = el.parentElement;
                if (!el || !el.href) return;

                var href = el.href;

                try { window.Android.onUserIntentNavigate(); } catch(e) {}
                try { window.Android.onSpaNavigation(href); } catch(e) {}

                e.preventDefault();
                e.stopImmediatePropagation();
            }, true);
        })();
    """.trimIndent()

    /**
     * SPA HOOK — оставлен только для НЕ‑поисковиков и НЕ‑книжных сайтов.
     * На книжных сайтах он больше не используется.
     */
    private val spaHookJs = """
        (function() {
            function notify(url, source) {
                try { console.log("[SPA-HOOK] " + source + " → " + url); } catch(e) {}
                try { window.Android.onSpaNavigation(url); } catch(e) {}
            }

            const originalPushState = history.pushState;
            history.pushState = function(state, title, url) {
                notify(url, "pushState");
                return originalPushState.apply(this, arguments);
            };

            const originalReplaceState = history.replaceState;
            history.replaceState = function(state, title, url) {
                notify(url, "replaceState");
                return originalReplaceState.apply(this, arguments);
            };

            window.addEventListener('popstate', function() {
                notify(location.href, "popstate");
            });

            const originalAssign = window.location.assign;
            window.location.assign = function(url) {
                notify(url, "location.assign");
                return originalAssign.call(window.location, url);
            };

            const originalReplace = window.location.replace;
            window.location.replace = function(url) {
                notify(url, "location.replace");
                return originalReplace.call(window.location, url);
            };
        })();
    """.trimIndent()

    fun injectSearchClickHook(webView: WebView) {
        webView.evaluateJavascript(searchClickHookJs, null)
    }

    fun injectBookClickHook(webView: WebView) {
        webView.evaluateJavascript(bookClickHookJs, null)
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
