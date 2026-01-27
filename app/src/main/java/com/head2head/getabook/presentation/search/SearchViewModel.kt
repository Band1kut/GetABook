package com.head2head.getabook.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.domain.usecase.BuildSearchUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _targetUrl = MutableStateFlow<String?>(null)
    val targetUrl: StateFlow<String?> = _targetUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showDownloadButton = MutableStateFlow(false)
    val showDownloadButton: StateFlow<Boolean> = _showDownloadButton

    private val _downloadProgress = MutableStateFlow<Int?>(null)
    val downloadProgress: StateFlow<Int?> = _downloadProgress

    fun loadUrl(query: String) {
        viewModelScope.launch {
            // 🔹 Первичная загрузка по запросу — тоже показываем оверлей
            _isLoading.value = true
            val url = buildSearchUrlUseCase(query)
            _targetUrl.value = url
        }
    }

    // 🔹 Клик пользователя (из JS) — мгновенно включаем оверлей
    fun onUserClick() {
        _isLoading.value = true
    }

    // 🔹 Реальное начало загрузки WebView — выключаем оверлей
    fun onPageStarted() {
        _isLoading.value = false
    }

    // 🔹 Можно оставить под будущее, но оверлей тут больше не трогаем
    fun onPageFinished() {
        // no-op или логика для чего-то ещё
    }

    fun onBookPageDetected(isBook: Boolean) {
        _showDownloadButton.value = isBook
    }

    fun requestDownload(url: String) {
        viewModelScope.launch {
            _downloadProgress.value = 0

            for (i in 1..100) {
                _downloadProgress.value = i
                kotlinx.coroutines.delay(10)
            }

            _downloadProgress.value = null
        }
    }
}

