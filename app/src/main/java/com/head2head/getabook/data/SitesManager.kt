package com.head2head.getabook.data

import android.content.Context
import com.head2head.getabook.data.model.AudioBookSite
import org.json.JSONObject
import android.util.Log

/**
 * Менеджер для работы с поддерживаемыми сайтами
 */
object SitesManager {
    // ConcurrentHashMap для потокобезопасности
    private val sitesMap = java.util.concurrent.ConcurrentHashMap<String, AudioBookSite>()

    /**
     * Инициализация при запуске приложения
     */
    fun initialize(context: Context) {
        loadSitesFromJson(context)
        Log.d("SitesManager", "Загружено ${sitesMap.size} сайтов: ${sitesMap.keys}")
    }

    /**
     * Загружает сайты из JSON файла
     */
    private fun loadSitesFromJson(context: Context) {
        try {
            val jsonString = context.assets.open("sites.json")
                .bufferedReader()
                .use { it.readText() }

            parseAsJsonObject(jsonString)
        } catch (e: Exception) {
            Log.e("SitesManager", "Ошибка загрузки sites.json", e)
            loadDefaultSites() // Fallback
        }
    }

    /**
     * Парсинг JSON как объекта {"domain": {"book_pattern": "..."}, ...}
     */
    private fun parseAsJsonObject(jsonString: String) {
        val jsonObject = JSONObject(jsonString)

        for (domain in jsonObject.keys()) {
            val config = jsonObject.getJSONObject(domain)
            val bookPattern = config.getString("book_pattern")

            sitesMap[domain] = AudioBookSite(
                domain = domain,
                bookPattern = bookPattern
            )

            Log.d("SitesManager", "Добавлен сайт: $domain -> $bookPattern")
        }
    }

    /**
     * Запасной вариант: сайты по умолчанию
     */
    private fun loadDefaultSites() {
        sitesMap.clear()

        sitesMap["knigavuhe.org"] = AudioBookSite(
            domain = "knigavuhe.org",
            bookPattern = ".book_header"
        )

        sitesMap["audiokniga.one"] = AudioBookSite(
            domain = "audiokniga.one",
            bookPattern = ".player-container"
        )

        Log.d("SitesManager", "Загружены сайты по умолчанию")
    }

    /**
     * Находит сайт по URL
     * Проходимся по ключам мапы и ищем вхождение ключа в строку
     */
    fun findSite(url: String): AudioBookSite? {
        // Ищем ключ (домен), который содержится в URL
        for ((domain, site) in sitesMap) {
            if (url.contains(domain, ignoreCase = true)) {
                return site
            }
        }
        return null
    }

    /**
     * Проверяет, поддерживается ли сайт
     */
    fun isSiteSupported(url: String): Boolean {
        return findSite(url) != null
    }

    /**
     * Получить все загруженные сайты
     */
    fun getAllSites(): List<AudioBookSite> {
        return sitesMap.values.toList()
    }

    /**
     * Получить домены всех сайтов
     */
    fun getAllDomains(): List<String> {
        return sitesMap.keys.toList()
    }
}