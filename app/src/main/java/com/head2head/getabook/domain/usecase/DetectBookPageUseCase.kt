package com.head2head.getabook.domain.usecase

import com.head2head.getabook.domain.model.AudioBookSite

/**
 * Use-case для определения, является ли текущий URL страницей книги.
 *
 * Сейчас логика простая — проверяем совпадение по регулярному выражению.
 * Позже можно расширить:
 * - проверка HTML-контента
 * - проверка наличия аудиофайлов
 * - разные правила для разных сайтов
 */
class DetectBookPageUseCase {

    operator fun invoke(site: AudioBookSite, url: String): Boolean {
        if (url.isBlank()) return false

        return try {
            val regex = Regex(site.bookPattern)
            regex.containsMatchIn(url)
        } catch (e: Exception) {
            false
        }
    }
}
