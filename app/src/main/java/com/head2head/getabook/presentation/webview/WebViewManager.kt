package com.head2head.getabook.presentation.webview

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.head2head.getabook.data.scripts.ScriptProvider
import dagger.hilt.android.qualifiers.ApplicationContext
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
    var onUserLinkClick: ((String) -> Unit)? = null

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

        // JS интерфейс
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onLinkClick(url: String) {
                onUserLinkClick?.invoke(url)
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
                    // Внедряем ловушку навигации
                    scriptProvider.injectNavigationHook(view)

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
}
