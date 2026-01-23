package com.head2head.getabook.domain.model

/**
 * Доменная модель сайта аудиокниг.
 *
 * На этом уровне — только данные и минимальная логика,
 * без зависимостей от Android или WebView.
 */
data class AudioBookSite(
    val domain: String,
    val bookPattern: String,
    val hideElement: List<String>
)
