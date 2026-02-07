package com.head2head.getabook.presentation.webview

import android.view.View
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewComponent(
    modifier: Modifier = Modifier,
    searchManager: SearchWebViewManager,
    bookManager: BookWebViewManager,
    isBookMode: Boolean,
    pendingBookUrl: String?, // ← добавляем URL книги
) {
    val context = LocalContext.current

    // Search WebView живёт всё время жизни экрана
    val searchWebView = remember {
        WebView(context).also { searchManager.attach(it) }
    }

    // Book WebView создаётся только когда включён режим книги
    val bookWebView = remember(isBookMode) {
        if (isBookMode) {
            WebView(context).also { bookManager.attach(it) }
        } else null
    }

    // Загружаем URL только когда WebView уже создан
    LaunchedEffect(bookWebView, pendingBookUrl) {
        if (bookWebView != null && pendingBookUrl != null) {
            bookManager.loadBookUrl(pendingBookUrl)
        }
    }

    Box(modifier = modifier) {

        // Search WebView
        AndroidView(
            factory = { searchWebView },
            update = { view ->
                view.visibility = if (!isBookMode) View.VISIBLE else View.GONE
            }
        )

        // Book WebView
        if (bookWebView != null) {
            AndroidView(
                factory = { bookWebView },
                update = { view ->
                    view.visibility = if (isBookMode) View.VISIBLE else View.GONE
                }
            )
        }
    }
}
