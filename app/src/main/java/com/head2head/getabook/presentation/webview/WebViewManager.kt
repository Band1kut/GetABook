package com.head2head.getabook.presentation.webview

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.ConsoleMessage
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

        webView.webViewClient = object : WebViewClient() {

            private val blockedHosts = listOf(
                "yadro.ru",
                "mc.yandex.ru",
                "googletagmanager.com",
                "adfinity.pro",
//                "redirectto.cc",
                "yandex.ru/ads",
                "tds.bid",
                "cdn.tds.bid",
                "googlesyndication.com",
                "pagead2.googlesyndication.com",
//                "cdn.jsdelivr.net/npm/yandex-share2"
            )

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {

                val url = request?.url?.toString() ?: return null

                // Проверяем, содержит ли URL один из заблокированных доменов
                if (blockedHosts.any { url.contains(it, ignoreCase = true) }) {
                    Log.d("WebViewBlock", "Blocked: $url")

                    // Возвращаем пустой ответ — запрос заблокирован
                    return WebResourceResponse(
                        "text/plain",
                        "utf-8",
                        null
                    )
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null && view != null) {
                    pageAnalyzer.handleEvent(url, PageEvent.Started, view)
                }

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url != null && view != null) {
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

