package com.head2head.getabook.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.domain.usecase.BuildSearchUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    val activeSitesRepository: ActiveSitesRepository,
    private val buildSearchUrlUseCase: BuildSearchUrlUseCase
) : ViewModel() {
    private val tag = "SearchVM"
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isBookMode = MutableStateFlow(false)
    val isBookMode: StateFlow<Boolean> = _isBookMode

    private val _navigateToBook = MutableStateFlow<String?>(null)
    val navigateToBook: StateFlow<String?> = _navigateToBook

    private val _loadError = MutableStateFlow(false)
    val loadError: StateFlow<Boolean> = _loadError

    private val _showDownloadButton = MutableStateFlow(false)
    val showDownloadButton: StateFlow<Boolean> = _showDownloadButton

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress

    private var loadTimeoutJob: Job? = null
    private var currentBookUrl: String? = null

    private val _pendingBookUrl = MutableStateFlow<String?>(null)
    val pendingBookUrl = _pendingBookUrl

    fun openBook(url: String) {
        _pendingBookUrl.value = url
        _isBookMode.value = true
    }


    suspend fun buildSearchUrl(query: String): String {
        return buildSearchUrlUseCase(query)
    }

    fun onSearchNavigate(url: String) {
        val site = activeSitesRepository.getSiteByUrl(url)
        if (site != null) {
            _navigateToBook.value = url
        }
    }

    fun onUserClick() {
        _isLoading.value = true
        startLoadTimeout()
    }

    fun onSearchPageStarted() {
        cancelLoadTimeout()
        _isLoading.value = false
        // остаёмся в поисковике
    }

    fun onBookPageStarted() {
        Log.d(tag, "onBookPageStarted() → entering book mode")
        cancelLoadTimeout()
        _isLoading.value = false
        _isBookMode.value = true   // ← переключение только здесь
    }

    fun onPageFinished() {
        // можно оставить пустым
    }

    fun onLoadTimeout() {
        _isLoading.value = false
        _loadError.value = true
        // НЕ переключаем режим, остаёмся там, где были
    }

    fun clearLoadError() {
        _loadError.value = false
    }

    private fun startLoadTimeout() {
        loadTimeoutJob?.cancel()
        loadTimeoutJob = viewModelScope.launch {
            delay(10_000)
            onLoadTimeout()
        }
    }

    private fun cancelLoadTimeout() {
        loadTimeoutJob?.cancel()
    }

    fun onBookPageDetected(isBook: Boolean) {
        _showDownloadButton.value = isBook
        if (!isBook) currentBookUrl = null
    }

    fun setCurrentBookUrl(url: String) {
        currentBookUrl = url
    }

    fun exitBookMode() {
        Log.d(tag, "exitBookMode()")
        _isBookMode.value = false
    }


    fun requestDownload() {
        val url = currentBookUrl ?: return

        viewModelScope.launch {
            _downloadProgress.value = 0
            for (i in 1..100) {
                delay(10)
                _downloadProgress.value = i
            }
            _downloadProgress.value = null
        }
    }
}
