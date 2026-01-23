package com.head2head.getabook.domain.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.head2head.getabook.data.settings.SearchEngine
import com.head2head.getabook.data.settings.SettingsKeys
import com.head2head.getabook.data.settings.settingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val context: Context,
    private val sitesRepository: SitesRepository
) {

    val searchEngineFlow: Flow<SearchEngine> =
        context.settingsDataStore.data.map { prefs ->
            when (prefs[SettingsKeys.SEARCH_ENGINE]) {
                SearchEngine.GOOGLE.name -> SearchEngine.GOOGLE
                else -> SearchEngine.YANDEX
            }
        }

    val downloadFolderFlow: Flow<String?> =
        context.settingsDataStore.data.map { prefs ->
            prefs[SettingsKeys.DOWNLOAD_FOLDER]
        }

    /**
     * Главная логика:
     * - Если пользователь ещё НИ РАЗУ не выбирал сайты → включаем ВСЕ сайты.
     * - Если пользователь явно выбрал пустой список → отдаём пустой список.
     */
    val enabledSitesFlow: Flow<Set<String>> =
        context.settingsDataStore.data.map { prefs ->
            val saved = prefs[SettingsKeys.ENABLED_SITES]

            if (saved == null) {
                // Пользователь ещё не выбирал — включаем ВСЕ сайты
                sitesRepository.getAllSites().map { it.domain }.toSet()
            } else {
                // Пользователь уже выбирал — отдаём как есть (включая пустой Set)
                saved
            }
        }

    suspend fun setSearchEngine(engine: SearchEngine) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.SEARCH_ENGINE] = engine.name
        }
    }

    suspend fun setDownloadFolder(path: String?) {
        context.settingsDataStore.edit { prefs ->
            if (path == null) {
                prefs.remove(SettingsKeys.DOWNLOAD_FOLDER)
            } else {
                prefs[SettingsKeys.DOWNLOAD_FOLDER] = path
            }
        }
    }

    suspend fun setEnabledSites(domains: Set<String>) {
        context.settingsDataStore.edit { prefs ->
            prefs[SettingsKeys.ENABLED_SITES] = domains
        }
    }
}