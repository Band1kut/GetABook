package com.head2head.getabook.presentation.webview

import android.view.View
import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Box

@Composable
fun WebViewComponent(
    modifier: Modifier = Modifier,
    webViewManager: WebViewManager
) {
    val context = LocalContext.current

    // --- REMEMBER SEARCH WEBVIEW ---
    val searchWebView = remember {
        WebView(context).apply {
            webViewManager.attachSearchWebView(this)
        }
    }

    // --- REMEMBER BOOK WEBVIEW ---
    val bookWebView = remember {
        WebView(context).apply {
            webViewManager.attachBookWebView(this)
        }
    }

    Box(modifier = modifier) {

        // --- SEARCH WEBVIEW ---
        AndroidView(
            factory = { searchWebView },
            update = { view ->
                view.visibility =
                    if (webViewManager.isSearchMode()) View.VISIBLE else View.GONE
            }
        )

        // --- BOOK WEBVIEW ---
        AndroidView(
            factory = { bookWebView },
            update = { view ->
                view.visibility =
                    if (webViewManager.isBookMode()) View.VISIBLE else View.GONE
            }
        )

        // --- CLEANUP ---
        DisposableEffect(Unit) {
            onDispose {
                webViewManager.destroyAll()
            }
        }
    }
}
