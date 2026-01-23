package com.head2head.getabook.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.head2head.getabook.presentation.webview.WebViewComponent
import com.head2head.getabook.presentation.webview.WebViewManager

@Composable
fun SearchScreen(
    query: String,
    webViewManager: WebViewManager,
    viewModel: SearchViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadUrl(query)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val showDownloadButton by viewModel.showDownloadButton.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val targetUrl by viewModel.targetUrl.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        WebViewComponent(
            modifier = Modifier.fillMaxSize(),
            webViewManager = webViewManager
        )

        LaunchedEffect(targetUrl) {
            targetUrl?.let { url ->
                webViewManager.loadUrl(url)
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (showDownloadButton) {
            FloatingActionButton(
                onClick = {
                    targetUrl?.let { viewModel.requestDownload(it) }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
        }

        downloadProgress?.let { progress ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f }
                )
            }
        }
    }
}
