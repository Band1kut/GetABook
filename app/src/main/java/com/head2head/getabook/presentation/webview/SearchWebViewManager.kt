package com.head2head.getabook.presentation.webview

import android.graphics.Bitmap
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.head2head.getabook.data.scripts.ScriptProvider

interface SearchWebViewManager {
    val webView: WebView

    var onNavigate: ((String) -> Unit)?
    var onPageStarted: (() -> Unit)?
    var onPageFinished: (() -> Unit)?

    fun attach(webView: WebView)
    fun loadSearchUrl(url: String)
    fun canGoBack(): Boolean
    fun goBack()
}

class SearchWebViewManagerImpl(
    private val scriptProvider: ScriptProvider
) : SearchWebViewManager {
    private val tag = "SearchManager"
    override lateinit var webView: WebView
        private set

    override var onNavigate: ((String) -> Unit)? = null
    override var onPageStarted: (() -> Unit)? = null
    override var onPageFinished: (() -> Unit)? = null

    override fun attach(webView: WebView) {
        this.webView = webView
        configure()
    }

    private fun configure() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.addJavascriptInterface(object {

            @JavascriptInterface
            fun onNavigate(url: String) {
                onNavigate?.invoke(url)
            }

        }, "Android")

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                onPageStarted?.invoke()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke()
                if (url != null && view != null && isSearchEngine(url)) {
                    scriptProvider.injectSearchClickHook(view)
                }
            }
        }
    }

    override fun loadSearchUrl(url: String) {
        Log.d(tag, "loadSearchUrl($url)")
        webView.loadUrl(url)
    }

    override fun canGoBack(): Boolean = webView.canGoBack()

    override fun goBack() {
        webView.goBack()
    }

    private fun isSearchEngine(url: String): Boolean {
        val u = url.lowercase()
        return u.contains("yandex.") || u.contains("ya.ru") || u.contains("google.")
    }
}
