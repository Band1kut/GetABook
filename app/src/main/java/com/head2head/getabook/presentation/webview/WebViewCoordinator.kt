package com.head2head.getabook.presentation.webview

import android.content.Context
import android.webkit.WebView
import com.head2head.getabook.presentation.search.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import android.util.Log

class WebViewCoordinator(
    private val context: Context,
    private val searchManager: SearchWebViewManager,
    private val bookManager: BookWebViewManager
) {

    private val scope = CoroutineScope(Dispatchers.Main)

    // ключ для пересоздания BookWebView
    private val _bookWebViewKey = MutableStateFlow(0)
    val bookWebViewKey = _bookWebViewKey

    private val tag = "Coordinator"

    fun bindToViewModel(viewModel: SearchViewModel) {
        Log.d(tag, "bindToViewModel()")

        searchManager.onNavigate = { url ->
            Log.d(tag, "SearchWebView navigate → $url")
            viewModel.onSearchNavigate(url)
        }

        searchManager.onPageStarted = {
            Log.d(tag, "SearchWebView onPageStarted")
            viewModel.onSearchPageStarted()
        }

        searchManager.onPageFinished = {
            Log.d(tag, "SearchWebView onPageFinished")
            viewModel.onPageFinished()
        }

        bookManager.onPageStarted = {
            Log.d(tag, "BookWebView onPageStarted")
            viewModel.onBookPageStarted()
        }

        bookManager.onPageFinished = {
            Log.d(tag, "BookWebView onPageFinished")
            viewModel.onPageFinished()
        }

        scope.launch {
            viewModel.resetBookWebView.collectLatest { shouldReset ->
                if (shouldReset) {
                    Log.d(tag, "resetBookWebView() requested")
                    resetBookWebView()
                    viewModel.acknowledgeBookWebViewReset()
                }
            }
        }

        scope.launch {
            viewModel.navigateToBook.collectLatest { url ->
                if (url != null) {
                    Log.d(tag, "navigateToBook → $url")
                    viewModel.onUserClick()
                    viewModel.setCurrentBookUrl(url)
                    bookManager.loadBookUrl(url)
                }
            }
        }
    }

    private fun resetBookWebView() {
        Log.d(tag, "Destroying old BookWebView")

        val old = bookManager.webView
        try {
            old.stopLoading()
            old.destroy()
        } catch (e: Throwable) {
            Log.e(tag, "Error destroying WebView", e)
        }

        Log.d(tag, "Creating new BookWebView")
        val newWebView = WebView(context)
        bookManager.attach(newWebView)

        _bookWebViewKey.value++
        Log.d(tag, "New BookWebView key = ${_bookWebViewKey.value}")
    }

}
