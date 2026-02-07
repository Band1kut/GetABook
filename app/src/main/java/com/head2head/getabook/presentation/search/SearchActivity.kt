package com.head2head.getabook.presentation.search

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.data.scripts.ScriptProvider
import com.head2head.getabook.presentation.webview.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : ComponentActivity() {

    @Inject
    lateinit var scriptProvider: ScriptProvider

    @Inject
    lateinit var activeSitesRepository: ActiveSitesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val query = intent.getStringExtra("query") ?: ""

        setContent {

            val viewModel: SearchViewModel = hiltViewModel()

            // --- 1. Создаём менеджеры WebView ---
            val searchManager = remember {
                SearchWebViewManagerImpl(scriptProvider)
            }

            val pageAnalyzer = remember {
                PageAnalyzer(
                    activeSitesRepository = activeSitesRepository,
                    scriptProvider = scriptProvider,
                    onBookPageDetected = { isBook ->
                        viewModel.onBookPageDetected(isBook)
                    }
                )
            }

            val bookManager = remember {
                BookWebViewManagerImpl(
                    scriptProvider = scriptProvider,
                    pageAnalyzer = pageAnalyzer
                )
            }

            // --- 2. Создаём координатор ---
            val coordinator = remember {
                WebViewCoordinator(
                    context = this,
                    searchManager = searchManager,
                    bookManager = bookManager
                )
            }

            // --- 3. Обработка кнопки "Назад" ---
            BackHandler {
                if (viewModel.isBookMode.value) {
                    if (bookManager.canGoBack()) {
                        bookManager.goBack()
                    } else {
                        // просто выходим из режима книги
                        viewModel.exitBookMode()
                    }
                } else {
                    if (searchManager.canGoBack()) {
                        searchManager.goBack()
                    } else {
                        finish()
                    }
                }
            }


            // --- 4. Запускаем экран ---
            SearchScreen(
                query = query,
                searchManager = searchManager,
                bookManager = bookManager,
                coordinator = coordinator,
                viewModel = viewModel
            )
        }
    }
}
