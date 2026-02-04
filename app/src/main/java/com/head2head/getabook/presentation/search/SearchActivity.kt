package com.head2head.getabook.presentation.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.head2head.getabook.presentation.webview.PageAnalyzer
import com.head2head.getabook.presentation.webview.WebViewManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : ComponentActivity() {

    @Inject
    lateinit var webViewManager: WebViewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val query = intent.getStringExtra("query") ?: ""

        setContent {

            val viewModel: SearchViewModel = hiltViewModel()

            val pageAnalyzer = remember {
                PageAnalyzer(
                    activeSitesRepository = viewModel.activeSitesRepository,
                    scriptProvider = webViewManager.scriptProvider,
                    onBookPageDetected = { isBook ->
                        viewModel.onBookPageDetected(isBook)
                    }
                )
            }

            webViewManager.setPageAnalyzer(pageAnalyzer)

            BackHandler {
                if (webViewManager.handleBack()) {
                    return@BackHandler
                } else {
                    finish()
                }
            }

            // Теперь загрузка поискового URL идёт напрямую через WebViewManager
//            webViewManager.loadSearchUrl(query)

            SearchScreen(
                query = query,
                webViewManager = webViewManager,
                viewModel = viewModel
            )
        }
    }
}
