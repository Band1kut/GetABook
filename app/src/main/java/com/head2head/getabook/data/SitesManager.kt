package com.head2head.getabook.data

import android.content.Context
import com.head2head.getabook.data.model.AudioBookSite
import org.json.JSONObject
import android.util.Log
import java.net.URLEncoder

object SitesManager {
    private val TAG = "SitesManager"
    private val sitesMap = java.util.concurrent.ConcurrentHashMap<String, AudioBookSite>()

    // Кэшированный шаблон поиска
    private lateinit var searchTemplate: String

    // Константы
    private const val YANDEX_MAX_QUERY_LENGTH = 400
    private const val YANDEX_SEARCH_BASE = "https://yandex.ru/search/?text="

    /**
     * Инициализация при запуске приложения
     */
    fun initialize(context: Context) {
        Log.i(TAG, "Начало инициализации SitesManager")

        val startTime = System.currentTimeMillis()

        try {
            // 1. Загружаем сайты из JSON
            loadSitesFromJson(context)

            // 2. Создаём шаблон поиска один раз
            createSearchTemplate()

            val loadTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "Инициализация завершена за ${loadTime}ms")
            Log.i(TAG, "Загружено ${sitesMap.size} сайтов: ${sitesMap.keys}")
            Log.d(TAG, "Шаблон поиска: ${searchTemplate.take(100)}...")

        } catch (e: Exception) {
            Log.e(TAG, "Критическая ошибка инициализации", e)
            loadDefaultSites()
            createSearchTemplate() // Повторяем создание шаблона
        }
    }

    /**
     * Создаёт шаблон поиска с закодированным фильтром сайтов
     */
    private fun createSearchTemplate() {
        val filter = sitesMap.keys.joinToString(" | ") { "site:$it" }
        Log.d(TAG, "Создание шаблона с фильтром: $filter")

        val encodedFilter = URLEncoder.encode(" $filter", "UTF-8")
        searchTemplate = "$YANDEX_SEARCH_BASE%s$encodedFilter"

        // Проверка длины фильтра
        if (encodedFilter.length > 200) {
            Log.w(TAG, "Фильтр сайтов длинный: ${encodedFilter.length} символов")
        }

        Log.d(TAG, "Шаблон создан, длина: ${searchTemplate.length} символов")
    }

    /**
     * Создаёт полный URL для поиска на Яндекс
     * @param rawQuery - сырой запрос пользователя
     * @return URL для поиска или fallback URL
     */
    fun buildSearchUrl(rawQuery: String): String {
        Log.d(TAG, "Построение URL для запроса: '${rawQuery.take(50)}...'")

        // 1. Очистка и валидация запроса
        val cleanQuery = rawQuery.trim()

        if (cleanQuery.isEmpty()) {
            Log.w(TAG, "Получен пустой запрос, возвращаем fallback")
            return "https://yandex.ru"
        }

        // 2. Проверка длины (400 символов для Яндекса)
        if (cleanQuery.length > YANDEX_MAX_QUERY_LENGTH) {
            Log.w(TAG, "Запрос слишком длинный: ${cleanQuery.length} > $YANDEX_MAX_QUERY_LENGTH")
            Log.i(TAG, "Обрезаем запрос до $YANDEX_MAX_QUERY_LENGTH символов")
        }

        val limitedQuery = cleanQuery.take(YANDEX_MAX_QUERY_LENGTH)

        // 3. Кодирование запроса
        val encodedQuery = try {
            URLEncoder.encode(limitedQuery, "UTF-8")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка кодирования запроса", e)
            URLEncoder.encode(limitedQuery, Charsets.UTF_8.name())
        }

        Log.d(TAG, "Запрос закодирован: ${encodedQuery.length} символов")

        // 4. Подстановка в шаблон
        val finalUrl = searchTemplate.replace("%s", encodedQuery)

        // 5. Проверка итоговой длины URL
        if (finalUrl.length > 2000) { // Практический лимит для браузеров
            Log.w(TAG, "Итоговый URL очень длинный: ${finalUrl.length} символов")
        }

        Log.i(TAG, "URL построен, длина: ${finalUrl.length} символов")
        Log.v(TAG, "Финальный URL (первые 150 символов): ${finalUrl.take(150)}...")

        return finalUrl
    }

    // =================== СУЩЕСТВУЮЩИЕ МЕТОДЫ С ЛОГИРОВАНИЕМ ===================

    private fun loadSitesFromJson(context: Context) {
        Log.d(TAG, "Загрузка sites.json из assets")

        try {
            val jsonString = context.assets.open("sites.json")
                .bufferedReader()
                .use { it.readText() }

            Log.d(TAG, "sites.json загружен, размер: ${jsonString.length} символов")
            parseAsJsonObject(jsonString)

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка загрузки sites.json", e)
            loadDefaultSites()
        }
    }

    private fun parseAsJsonObject(jsonString: String) {
        Log.d(TAG, "Парсинг JSON конфигурации сайтов")

        val jsonObject = JSONObject(jsonString)
        val siteCount = jsonObject.length()

        Log.i(TAG, "Найдено $siteCount сайтов в конфигурации")

        for (domain in jsonObject.keys()) {
            val config = jsonObject.getJSONObject(domain)
            val bookPattern = config.getString("book_pattern")

            sitesMap[domain] = AudioBookSite(
                domain = domain,
                bookPattern = bookPattern
            )

            Log.v(TAG, "Добавлен сайт: $domain -> $bookPattern")
        }
    }

    private fun loadDefaultSites() {
        Log.w(TAG, "Используем сайты по умолчанию")

        sitesMap.clear()

        val defaultSites = mapOf(
            "knigavuhe.org" to ".book_header",
            "audiokniga.one" to ".player-container"
        )

        defaultSites.forEach { (domain, pattern) ->
            sitesMap[domain] = AudioBookSite(domain, pattern)
            Log.d(TAG, "Добавлен сайт по умолчанию: $domain")
        }

        Log.i(TAG, "Загружено ${sitesMap.size} сайтов по умолчанию")
    }

    fun findSite(url: String): AudioBookSite? {
        Log.v(TAG, "Поиск сайта для URL: $url")

        for ((domain, site) in sitesMap) {
            if (url.contains(domain, ignoreCase = true)) {
                Log.d(TAG, "Найден сайт: $domain для URL: $url")
                return site
            }
        }

        Log.d(TAG, "Сайт не найден для URL: $url")
        return null
    }

    fun isSiteSupported(url: String): Boolean {
        val isSupported = findSite(url) != null
        Log.v(TAG, "Проверка поддержки URL '$url': $isSupported")
        return isSupported
    }

    fun getAllSites(): List<AudioBookSite> {
        Log.v(TAG, "Запрос всех сайтов, количество: ${sitesMap.size}")
        return sitesMap.values.toList()
    }

    fun getAllDomains(): List<String> {
        return sitesMap.keys.toList()
    }

    /**
     * Получить количество загруженных сайтов
     */
    fun getSiteCount(): Int {
        return sitesMap.size
    }
}