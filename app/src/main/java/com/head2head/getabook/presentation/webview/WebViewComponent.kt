package com.head2head.getabook.presentation.webview

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

import com.head2head.getabook.presentation.webview.WebViewManager


@Composable
fun WebViewComponent(
    modifier: Modifier = Modifier,
    webViewManager: WebViewManager
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).also { webView ->
                webViewManager.attachWebView(webView)
            }
        }
    )
}

