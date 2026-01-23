package com.head2head.getabook.data.active

import com.head2head.getabook.domain.repository.SettingsRepository
import com.head2head.getabook.domain.model.AudioBookSite
import com.head2head.getabook.domain.repository.SitesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class ActiveSitesRepository(
    private val sitesRepository: SitesRepository,
    private val settingsRepository: SettingsRepository
) {

    // Текущее состояние активных сайтов в памяти
    @Volatile
    private var activeSitesCache: List<AudioBookSite> = emptyList()

    // Поток активных сайтов (для UI или других слоёв)
    // Комбинируем:
    // 1) все сайты
    // 2) включённые домены из DataStore
    val activeSitesFlow: Flow<List<AudioBookSite>> = settingsRepository.enabledSitesFlow
        .map { enabledDomains ->
            val allSites = sitesRepository.getAllSites()

            // Если пользователь ещё не выбирал сайты → считаем, что все включены
            if (enabledDomains.isEmpty()) {
                allSites
            } else {
                allSites.filter { it.domain in enabledDomains }
            }
        }
        .onEach { activeSitesCache = it } // обновляем кэш
        .flowOn(Dispatchers.Default)
        .shareIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Eagerly,
            replay = 1
        )

    // Метод для PageAnalyzer — ищем сайт по URL только среди активных
//    fun getSiteByUrl(url: String): AudioBookSite? {
//        return activeSitesCache.firstOrNull { site ->
//            url.contains(site.domain, ignoreCase = true)
//        }
//    }

    fun getSiteByUrl(url: String): AudioBookSite? {
        val host = url.toUri().host ?: return null
        return activeSitesCache.firstOrNull { site ->
            host.equals(site.domain, ignoreCase = true) ||
                    host.endsWith("." + site.domain, ignoreCase = true)
        }
    }

}
