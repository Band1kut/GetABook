package com.head2head.getabook.domain.usecase

import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.data.settings.SearchEngine
import com.head2head.getabook.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.net.URLEncoder
import javax.inject.Inject

class BuildSearchUrlUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val activeSitesRepository: ActiveSitesRepository
) {

    suspend operator fun invoke(query: String): String {
        // 1. Читаем поисковик из DataStore
        val engine = settingsRepository.searchEngineFlow.first()

        // 2. Кодируем запрос
        val encodedQuery = URLEncoder.encode(query, "UTF-8")

        // 3. Берём активные сайты
        val activeSites = activeSitesRepository.activeSitesFlow.first()

        // 4. Если активных сайтов нет — ищем без фильтра
        if (activeSites.isEmpty()) {
            return when (engine) {
                SearchEngine.GOOGLE ->
                    "https://www.google.com/search?q=$encodedQuery"

                SearchEngine.YANDEX ->
                    "https://yandex.ru/search/?text=$encodedQuery"
            }
        }

        // 5. Формируем фильтр: site:domain1 | site:domain2
        val filter = activeSites.joinToString(" | ") { "site:${it.domain}" }

        // 6. Кодируем фильтр
        val encodedFilter = URLEncoder.encode(" $filter", "UTF-8")

        // 7. Собираем финальный URL
        return when (engine) {
            SearchEngine.GOOGLE ->
                "https://www.google.com/search?q=$encodedQuery$encodedFilter"

            SearchEngine.YANDEX ->
                "https://yandex.ru/search/?text=$encodedQuery$encodedFilter"
        }
    }
}
