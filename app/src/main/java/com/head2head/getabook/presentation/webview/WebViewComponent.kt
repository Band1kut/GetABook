package com.head2head.getabook.presentation.webview

import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewComponent(
    modifier: Modifier = Modifier,
    searchManager: SearchWebViewManager,
    bookManager: BookWebViewManager,
    isBookMode: Boolean,
    bookWebViewKey: Int
) {
    val context = LocalContext.current

    val searchWebView = remember {
        WebView(context).also { searchManager.attach(it) }
    }


    val bookWebView = remember(bookWebViewKey) {
        Log.d("WebViewComponent", "Recompose, bookKey=$bookWebViewKey")
        WebView(context).also { bookManager.attach(it) }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { searchWebView },
            update = { view ->
                view.visibility = if (!isBookMode) View.VISIBLE else View.GONE
            }
        )

        AndroidView(
            factory = { bookWebView },
            update = { view ->
                view.visibility = if (isBookMode) View.VISIBLE else View.GONE
            }
        )
    }
}
