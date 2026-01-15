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

class SearchViewModel : ViewModel() {
    // Состояния UI (только необходимые)
    private val _showDownloadButton = MutableStateFlow(false)
    val showDownloadButton: StateFlow<Boolean> = _showDownloadButton.asStateFlow()

    private val _currentSite = MutableStateFlow<AudioBookSite?>(null)
    val currentSite: StateFlow<AudioBookSite?> = _currentSite.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Добавим позже для прогресса загрузки:
    // private val _downloadProgress = MutableStateFlow(0f)
    // val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    // private val _isDownloading = MutableStateFlow(false)
    // val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    /**
     * Обработка загруженной страницы
     */
    fun onPageFinished(webView: WebView, url: String) {
        viewModelScope.launch {
            _isLoading.value = false

            // 1. Ищем сайт по URL
            val site = SitesManager.findSite(url)

            if (site != null) {
                Log.d("SearchViewModel", "Найден сайт: ${site.domain}")
                _currentSite.value = site

                // 2. Проверяем, страница ли это книги
                checkIfBookPage(webView, site)
            } else {
                Log.d("SearchViewModel", "Сайт не поддерживается: $url")
                _currentSite.value = null
                _showDownloadButton.value = false
            }
        }
    }

    /**
     * Проверка, является ли страница книгой
     */
    private fun checkIfBookPage(webView: WebView, site: AudioBookSite) {
        site.isBookPage(webView) { isBookPage ->
            _showDownloadButton.value = isBookPage

            if (isBookPage) {
                Log.i("SearchViewModel", "Обнаружена страница книги: ${site.domain}")
                // Здесь позже будет: авто-парсинг или подготовка к скачиванию
            }
        }
    }

    /**
     * Начало загрузки страницы
     */
    fun onPageStarted(url: String) {
        _isLoading.value = true
        _showDownloadButton.value = false // Скрываем кнопку при переходе
        Log.d("SearchViewModel", "Начинается загрузка: $url")
    }

    /**
     * Нажатие кнопки скачивания
     */
    fun onDownloadClicked() {
        val site = _currentSite.value
        if (site != null && _showDownloadButton.value) {
            Log.i("SearchViewModel", "Начало скачивания с ${site.domain}")
            // TODO: Запуск парсинга и скачивания

            // БУДУЩАЯ ЛОГИКА:
            // 1. Показать индикатор прогресса на кнопке
            // _isDownloading.value = true
            // 2. Запустить парсинг
            // 3. Начать загрузку файлов
            // 4. Обновлять _downloadProgress
        }
    }

    /**
     * Сброс состояния
     */
    fun resetState() {
        _showDownloadButton.value = false
        _currentSite.value = null
        _isLoading.value = false
    }
}