package com.head2head.getabook.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val isLoading = viewModel.isLoading.collectAsState()
    val showDownloadButton = viewModel.showDownloadButton.collectAsState()
    val downloadProgress = viewModel.downloadProgress.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        WebViewComponent(
            onPageStarted = { viewModel.setLoading(true) },
            onPageFinished = { viewModel.setLoading(false) },
            onBookDetected = { viewModel.setShowDownloadButton(true) },
            onBuildUrl = { query -> viewModel.buildSearchUrl(query) },
            targetUrl = viewModel.targetUrl,
            onUrlChanged = { url -> viewModel.onUrlChanged(url) }
        )

        if (isLoading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (showDownloadButton.value) {
            FloatingActionButton(
                onClick = {
                    val currentUrl = viewModel.targetUrl.value
                    if (currentUrl != null) {
                        viewModel.requestDownload(currentUrl)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download"
                )
            }
        }

        if (downloadProgress.value != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = (downloadProgress.value ?: 0) / 100f
                )
            }
        }
    }
}
