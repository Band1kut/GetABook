package com.head2head.getabook.presentation.webview

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.head2head.getabook.data.scripts.ScriptProvider

interface BookWebViewManager {
    val webView: WebView

    var onPageStarted: (() -> Unit)?
    var onPageFinished: (() -> Unit)?
    var onBookDetected: ((Boolean) -> Unit)?

    fun attach(webView: WebView)
    fun loadBookUrl(url: String)
    fun canGoBack(): Boolean
    fun goBack()
}

class BookWebViewManagerImpl(
    private val scriptProvider: ScriptProvider,
    private val pageAnalyzer: PageAnalyzer
) : BookWebViewManager {
    private val tag = "BookManager"
    override lateinit var webView: WebView
        private set

    override var onPageStarted: (() -> Unit)? = null
    override var onPageFinished: (() -> Unit)? = null
    override var onBookDetected: ((Boolean) -> Unit)? = null

    override fun attach(webView: WebView) {
        Log.d(tag, "attach() new WebView = $webView")
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

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                onPageStarted?.invoke()
                if (url != null && view != null) {
                    pageAnalyzer.handleEvent(url, PageEvent.Started, view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                onPageFinished?.invoke()
                if (url != null && view != null) {
                    scriptProvider.injectBookClickHook(view)
                    pageAnalyzer.handleEvent(url, PageEvent.Finished, view)
                }
//                webView.clearHistory()
            }
        }

    }

    override fun loadBookUrl(url: String) {
        Log.d(tag, "loadBookUrl($url)")
        webView.loadUrl(url)
    }

    override fun canGoBack(): Boolean = webView.canGoBack()

    override fun goBack() {
        webView.goBack()
    }

//    fun resetHistory() {
//        webView.loadUrl("about:blank")
////        webView.clearHistory()
//    }

}

// после срабатывания таймаута страница загрузилась!?