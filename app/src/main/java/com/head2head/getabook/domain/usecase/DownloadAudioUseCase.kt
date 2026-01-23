package com.head2head.getabook.domain.usecase

import com.head2head.getabook.data.download.DownloadManager
import kotlinx.coroutines.flow.Flow

/**
 * Use-case для скачивания аудиофайла.
 *
 * Позже здесь появится логика:
 * - выбора имени файла
 * - выбора директории
 * - обработки ошибок
 * - ретраев
 */
class DownloadAudioUseCase(
    private val downloadManager: DownloadManager
) {

    operator fun invoke(url: String): Flow<Int> {
        return downloadManager.download(url)
    }
}
