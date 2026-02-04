package com.head2head.getabook.presentation.webview

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.data.scripts.ScriptProvider
import com.head2head.getabook.domain.usecase.BuildSearchUrlUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val scriptProvider: ScriptProvider,
    private val blockedHosts: Set<String>,
    private val buildSearchUrlUseCase: BuildSearchUrlUseCase,
    private val activeSitesRepository: ActiveSitesRepository
) {

    private enum class Mode { SEARCH, BOOK }

    private var searchWebViewRef: WeakReference<WebView>? = null
    private var bookWebViewRef: WeakReference<WebView>? = null

    private var mode: Mode = Mode.SEARCH

    private lateinit var pageAnalyzer: PageAnalyzer

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

    // region Attach

    fun attachSearchWebView(view: WebView) {
        searchWebViewRef = WeakReference(view)
        configureSearchWebView(view)
        view.visibility = if (mode == Mode.SEARCH) View.VISIBLE else View.GONE
    }

    fun attachBookWebView(view: WebView) {
        bookWebViewRef = WeakReference(view)
        configureBookWebView(view)
        view.visibility = if (mode == Mode.BOOK) View.VISIBLE else View.GONE
    }

    // endregion

    // region Public API

    fun isSearchMode() = mode == Mode.SEARCH
    fun isBookMode() = mode == Mode.BOOK

    suspend fun loadSearchQuery(query: String) {
        val url = buildSearchUrlUseCase(query)
        mode = Mode.SEARCH

        getSearchWebView()?.apply {
            visibility = View.VISIBLE
            getBookWebView()?.visibility = View.GONE
            loadUrl(url)
        }
    }

    fun switchToBook(url: String) {
        mode = Mode.BOOK
        getSearchWebView()?.visibility = View.GONE
        getBookWebView()?.apply {
            visibility = View.VISIBLE
            loadUrl(url)
        }
    }

    fun switchToSearch() {
        mode = Mode.SEARCH
        getSearchWebView()?.visibility = View.VISIBLE
        getBookWebView()?.visibility = View.GONE
    }

    fun canGoBack(): Boolean {
        if (isLoadTimeoutActive) return true

        return when (mode) {
            Mode.SEARCH -> getSearchWebView()?.canGoBack() == true
            Mode.BOOK -> getBookWebView()?.canGoBack() == true
        }
    }

    fun handleBack(): Boolean {
        if (isLoadTimeoutActive) {
            forceStopLoading()
            return true
        }

        return when (mode) {
            Mode.SEARCH -> {
                val wv = getSearchWebView()
                if (wv?.canGoBack() == true) {
                    wv.goBack()
                    true
                } else false
            }

            Mode.BOOK -> {
                val wv = getBookWebView()
                if (wv?.canGoBack() == true) {
                    wv.goBack()
                    true
                } else {
                    switchToSearch()
                    true
                }
            }
        }
    }

    fun destroyAll() {
        cancelLoadTimeout()

        getSearchWebView()?.apply {
            stopLoadingSafely()
            destroy()
        }
        getBookWebView()?.apply {
            stopLoadingSafely()
            destroy()
        }

        searchWebViewRef = null
        bookWebViewRef = null
    }

    // endregion

    // region Internal helpers

    private fun getSearchWebView() = searchWebViewRef?.get()
    private fun getBookWebView() = bookWebViewRef?.get()

    private fun WebView.stopLoadingSafely() {
        try { stopLoading() } catch (_: Exception) {}
    }

    private fun isBookSite(url: String): Boolean {
        return activeSitesRepository.getSiteByUrl(url) != null
    }

    // endregion

    // region Configuration

    private fun configureCommonSettings(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                Log.d("WebViewConsole", message?.message() ?: "")
                return true
            }
        }
    }

    private fun configureSearchWebView(webView: WebView) {
        configureCommonSettings(webView)

        webView.addJavascriptInterface(object {

            @JavascriptInterface
            fun onUserIntentNavigate(url: String) {
                Log.d("WebViewManager", "SearchWebView navigate: $url")

                if (isBookSite(url)) {
                    switchToBook(url)
                } else {
                    onUserClick?.invoke()
                    startLoadTimeout()
                }
            }

            @JavascriptInterface
            fun onSpaNavigation(relativeUrl: String) {
                webView.post {
                    val current = webView.url ?: return@post
                    if (!isSearchEngine(current)) return@post

                    val absolute = makeAbsoluteUrl(current, relativeUrl)
                    webView.loadUrl(absolute)
                }
            }

        }, "Android")

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                cancelLoadTimeout()
                onPageStarted?.invoke()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                cancelLoadTimeout()
                onPageFinished?.invoke()

                if (url != null && view != null && isSearchEngine(url)) {
                    scriptProvider.injectSearchClickHook(view)
                }
            }
        }
    }

    private fun configureBookWebView(webView: WebView) {
        configureCommonSettings(webView)

        webView.addJavascriptInterface(object {

            @JavascriptInterface
            fun onUserIntentNavigate(url: String) {
                onUserClick?.invoke()
                startLoadTimeout()
            }

            @JavascriptInterface
            fun onSpaNavigation(relativeUrl: String) {
                webView.post {
                    val current = webView.url ?: return@post
                    if (isSearchEngine(current)) return@post

                    val absolute = makeAbsoluteUrl(current, relativeUrl)
                    webView.loadUrl(absolute)
                }
            }

        }, "Android")

        webView.webViewClient = object : WebViewClient() {

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
                        fullUrl.contains(ruleLower)
                    } else {
                        cleanHost == ruleLower || cleanHost.endsWith(".$ruleLower")
                    }
                }

                if (shouldBlock) {
                    Log.d("AdBlock", "Blocked: $fullUrl")
                    return WebResourceResponse("text/plain", "utf-8", null)
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                cancelLoadTimeout()
                onPageStarted?.invoke()

                if (url != null && view != null) {
                    pageAnalyzer.handleEvent(url, PageEvent.Started, view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                cancelLoadTimeout()
                onPageFinished?.invoke()

                if (url != null && view != null) {
                    scriptProvider.injectBookClickHook(view)
                    pageAnalyzer.handleEvent(url, PageEvent.Finished, view)
                }
            }
        }
    }

    // endregion

    // region Timeout

    private fun startLoadTimeout() {
        loadTimeoutRunnable?.let { getCurrentWebView()?.removeCallbacks(it) }

        isLoadTimeoutActive = true

        val r = Runnable {
            forceStopLoading()
            Toast.makeText(
                context,
                "Страница не загружена, попробуйте ещё раз",
                Toast.LENGTH_LONG
            ).show()
        }

        loadTimeoutRunnable = r
        getCurrentWebView()?.postDelayed(r, loadTimeoutMs)
    }

    private fun cancelLoadTimeout() {
        loadTimeoutRunnable?.let { getCurrentWebView()?.removeCallbacks(it) }
        isLoadTimeoutActive = false
    }

    private fun forceStopLoading() {
        cancelLoadTimeout()

        val wv = getCurrentWebView() ?: return
        try { wv.stopLoading() } catch (_: Exception) {}

        val fallback = wv.copyBackForwardList().currentItem?.url
        if (fallback != null) wv.loadUrl(fallback)

        onForceStopLoading?.invoke()
    }

    private fun getCurrentWebView() =
        if (mode == Mode.SEARCH) getSearchWebView() else getBookWebView()

    // endregion

    // region Utils

    private fun makeAbsoluteUrl(currentUrl: String, relative: String): String =
        try {
            val base = URL(currentUrl)
            URL(base, relative).toString()
        } catch (e: Exception) {
            currentUrl
        }

    private fun isSearchEngine(url: String): Boolean {
        val u = url.lowercase()
        return u.contains("yandex.") ||
                u.contains("ya.ru") ||
                u.contains("google.")
    }

    // endregion
}
