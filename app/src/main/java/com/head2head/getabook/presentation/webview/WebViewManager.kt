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
import android.widget.Toast
import com.head2head.getabook.data.scripts.ScriptProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    val scriptProvider: ScriptProvider,
    private val blockedHosts: Set<String>   // ← внедрено через DI
) {

    private lateinit var pageAnalyzer: PageAnalyzer
    private lateinit var webView: WebView

    var onUserClick: (() -> Unit)? = null
    var onPageStarted: (() -> Unit)? = null
    var onPageFinished: (() -> Unit)? = null
    var onForceStopLoading: (() -> Unit)? = null

    private var loadTimeoutRunnable: Runnable? = null
    private val loadTimeoutMs = 15000L

    private var isLoadTimeoutActive = false

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

            @JavascriptInterface
            fun onUserIntentNavigate() {
                Log.d("WebViewManager", "onUserIntentNavigate")
                onUserClick?.invoke()
                startLoadTimeout()
            }

            @JavascriptInterface
            fun onSpaNavigation(relativeUrl: String) {
                Log.d("WebViewManager", "onSpaNavigation IN = $relativeUrl")

                webView.post {
                    val current = webView.url ?: return@post

                    if (isSearchEngine(current)) return@post

                    val absolute = makeAbsoluteUrl(current, relativeUrl)
                    Log.d("WebViewManager", "loadUrl = $absolute")
                    webView.loadUrl(absolute)
                }
            }

        }, "Android")

        webView.webViewClient = object : WebViewClient() {

            // -----------------------------
            // AD BLOCK
            // -----------------------------
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {

                val url = request?.url ?: return null
                val fullUrl = url.toString().lowercase()
                val host = url.host?.lowercase() ?: return null
                val cleanHost = host.removePrefix("www.")

                val shouldBlock = blockedHosts.any { rule ->
                    val ruleLower = rule.lowercase()

                    if (ruleLower.contains("/")) {
                        // Правило по пути: yandex.ru/ads
                        fullUrl.contains(ruleLower)
                    } else {
                        // Правило по домену: googlesyndication.com
                        cleanHost == ruleLower || cleanHost.endsWith(".$ruleLower")
                    }
                }

                if (shouldBlock) {
                    Log.d("AdBlock", "Blocked: $fullUrl")
                    return WebResourceResponse("text/plain", "utf-8", null)
                }

                return super.shouldInterceptRequest(view, request)
            }


            // -----------------------------
            // PAGE EVENTS
            // -----------------------------
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                cancelLoadTimeout()
                onPageStarted?.invoke()

                if (url != null && view != null) {
                    pageAnalyzer.handleEvent(url, PageEvent.Started, view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                cancelLoadTimeout()
                onPageFinished?.invoke()

                if (url != null && view != null) {
                    Log.d("WebViewManager", "onPageFinished url = $url")

                    if (isSearchEngine(url)) {
                        scriptProvider.injectSearchClickHook(view)
                    } else {
                        scriptProvider.injectBookClickHook(view)
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

    private fun startLoadTimeout() {
        loadTimeoutRunnable?.let { webView.removeCallbacks(it) }

        isLoadTimeoutActive = true

        val r = Runnable {
            Log.d("WebViewManager", "Timeout: page did not start loading")
            forceStopLoading()
            Toast.makeText(
                context,
                "Страница не загружена, попробуйте еще раз или другую ссылку",
                Toast.LENGTH_LONG
            ).show()
        }

        loadTimeoutRunnable = r
        webView.postDelayed(r, loadTimeoutMs)
    }

    private fun cancelLoadTimeout() {
        loadTimeoutRunnable?.let { webView.removeCallbacks(it) }
        isLoadTimeoutActive = false
    }

    private fun forceStopLoading() {
        cancelLoadTimeout()

        try { webView.stopLoading() } catch (_: Exception) {}

        val fallbackUrl = webView.copyBackForwardList().currentItem?.url

        if (fallbackUrl != null) {
            webView.loadUrl(fallbackUrl)
        }

        onForceStopLoading?.invoke()
    }

    fun loadUrl(url: String) {
        webView.loadUrl(url)
    }

    fun canGoBack(): Boolean {
        if (isLoadTimeoutActive){
            return true
        }
        return webView.canGoBack()
    }

    fun goBack() {
        if (isLoadTimeoutActive){
            forceStopLoading()
            return
        }
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
