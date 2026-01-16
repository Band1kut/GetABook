package com.head2head.getabook.search

import android.util.Log
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.head2head.getabook.data.SitesManager
import com.head2head.getabook.data.model.AudioBookSite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class SearchViewModel : ViewModel() {
    private val TAG = "SearchViewModel"

    private val _showDownloadButton = MutableStateFlow(false)
    val showDownloadButton: StateFlow<Boolean> = _showDownloadButton.asStateFlow()

    private val _currentSite = MutableStateFlow<AudioBookSite?>(null)
    val currentSite: StateFlow<AudioBookSite?> = _currentSite.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Начинаем с true для первой загрузки
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Дополнительное состояние для плавного показа
    private val _isContentReady = MutableStateFlow(false)
    val isContentReady: StateFlow<Boolean> = _isContentReady.asStateFlow()

    /**
     * Обработка загруженной страницы
     */
    fun onPageFinished(webView: WebView, url: String) {
        viewModelScope.launch {
            Log.d(TAG, "Страница загружена, начинаем обработку")

            // 1. Ищем сайт по URL
            val site = SitesManager.findSite(url)

            if (site != null) {
                Log.d(TAG, "Найден сайт: ${site.domain}")
                _currentSite.value = site

                // 2. Проверяем, страница ли это книги
                checkIfBookPage(webView, site)
            } else {
                Log.d(TAG, "Сайт не поддерживается: $url")
                _currentSite.value = null
                _showDownloadButton.value = false
            }

            // 3. Для Яндекс даём дополнительную задержку для CSS
            if (url.contains("yandex.ru", ignoreCase = true)) {
                delay(300) // Задержка для гарантированного применения CSS
            }

            // 4. Обновляем состояния
            _isLoading.value = false
            _isContentReady.value = true

            Log.d(TAG, "Обработка страницы завершена")
        }
    }

    /**
     * Проверка, является ли страница книгой
     */
    private fun checkIfBookPage(webView: WebView, site: AudioBookSite) {
        site.isBookPage(webView) { isBookPage ->
            _showDownloadButton.value = isBookPage

            if (isBookPage) {
                Log.i(TAG, "Обнаружена страница книги: ${site.domain}")
            }
        }
    }

    /**
     * Начало загрузки страницы
     */
    fun onPageStarted(url: String) {
        Log.d(TAG, "Начинается загрузка: $url")
        // Только сбрасываем флаг готовности, isLoading остаётся true
        _isContentReady.value = false
        _showDownloadButton.value = false // Скрываем кнопку при переходе
    }

    /**
     * Нажатие кнопки скачивания
     */
    fun onDownloadClicked() {
        val site = _currentSite.value
        if (site != null && _showDownloadButton.value) {
            Log.i(TAG, "Начало скачивания с ${site.domain}")
        }
    }

    /**
     * Сброс состояния
     */
    fun resetState() {
        _showDownloadButton.value = false
        _currentSite.value = null
        _isLoading.value = true
        _isContentReady.value = false
    }
}