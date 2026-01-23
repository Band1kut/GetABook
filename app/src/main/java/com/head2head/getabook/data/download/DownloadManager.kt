package com.head2head.getabook.data.download

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Новый DownloadManager.
 *
 * Пока это только каркас, который позже будет:
 * - скачивать файл
 * - отдавать прогресс
 * - обрабатывать ошибки
 * - сохранять файл в память
 */
class DownloadManager {

    /**
     * Загружает файл по URL.
     * Пока возвращает фейковый прогресс.
     */
    fun download(url: String): Flow<Int> = flow {
        emit(0)
        emit(25)
        emit(50)
        emit(75)
        emit(100)
    }
}
