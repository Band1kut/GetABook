package com.head2head.getabook.data.scripts

import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptProvider @Inject constructor() {

    /**
     * Универсальная ловушка навигации:
     * - клики по ссылкам
     * - history.pushState / replaceState
     * - popstate
     * - location.assign / replace
     * - прямое присваивание location = ...
     */
    private val navigationHookJs = """
        (function() {
            function notify(url) {
                try { window.Android.onLinkClick(url); } catch(e) {}
            }

            // 1. Клики по ссылкам
            document.addEventListener('click', function(e) {
                let el = e.target;
                while (el && el.tagName !== 'A') el = el.parentElement;
                if (el && el.href) notify(el.href);
            }, true);

            // 2. pushState / replaceState
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

            // 3. popstate
            window.addEventListener('popstate', function() {
                notify(location.href);
            });

            // 4. location.assign / replace
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

    fun injectNavigationHook(webView: WebView) {
        webView.evaluateJavascript(navigationHookJs, null)
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
