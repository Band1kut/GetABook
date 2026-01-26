package com.head2head.getabook.presentation.webview

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.head2head.getabook.data.scripts.ScriptProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val scriptProvider: ScriptProvider
) {

    private lateinit var pageAnalyzer: PageAnalyzer
    private lateinit var webView: WebView

    var onPageStarted: (() -> Unit)? = null
    var onPageFinished: (() -> Unit)? = null

    fun setPageAnalyzer(analyzer: PageAnalyzer) {
        this.pageAnalyzer = analyzer
    }

    fun attachWebView(view: WebView) {
        this.webView = view
        configureWebView()
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.addJavascriptInterface(object {

            /**
             * CLICK HOOK — ранняя индикация
             */
            @JavascriptInterface
            fun onUserIntentNavigate() {
                onPageStarted?.invoke()
            }

            /**
             * SPA HOOK — только для НЕ‑поисковиков
             */
            @JavascriptInterface
            fun onSpaNavigation(relativeUrl: String) {
                val current = webView.url ?: return

                if (isSearchEngine(current)) {
                    return
                }

                webView.post {
                    val absolute = makeAbsoluteUrl(current, relativeUrl)
                    webView.loadUrl(absolute)
                }
            }

        }, "Android")

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageStarted?.invoke()

                if (url != null && view != null) {
                    pageAnalyzer.handleEvent(url, PageEvent.Started, view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke()

                if (url != null && view != null) {

                    // CLICK HOOK — всегда
                    scriptProvider.injectClickHook(view)

                    // SPA HOOK — только если НЕ поисковик
                    if (!isSearchEngine(url)) {
                        scriptProvider.injectSpaHook(view)
                    }

                    pageAnalyzer.handleEvent(url, PageEvent.Finished, view)
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                Log.d("WebViewConsole", "${message?.message()}")
                return true
            }
        }
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun canGoBack(): Boolean = webView.canGoBack()

    fun goBack() {
        webView.goBack()
    }

    private fun makeAbsoluteUrl(currentUrl: String, relative: String): String {
        return try {
            val base = URL(currentUrl)
            URL(base, relative).toString()
        } catch (e: Exception) {
            currentUrl
        }
    }

    private fun isSearchEngine(url: String): Boolean {
        val u = url.lowercase()
        return u.contains("yandex.") ||
                u.contains("ya.ru") ||
                u.contains("google.")
    }
}
