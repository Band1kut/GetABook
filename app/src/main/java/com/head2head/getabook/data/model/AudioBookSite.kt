package com.head2head.getabook.data.model

data class AudioBookSite(
    val domain: String,
    val bookPattern: String  // CSS-селектор (например ".book_header")
) {
    /**
     * Проверяет, является ли страница книгой через JavaScript
     * @param webView WebView для выполнения JavaScript
     * @param callback Функция обратного вызова с результатом
     */
    fun isBookPage(
        webView: android.webkit.WebView,
        callback: (Boolean) -> Unit
    ) {
        // JavaScript для проверки наличия элемента по селектору
        val js = """
            (function() {
                var element = document.querySelector('$bookPattern');
                return element !== null;
            })()
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            // result будет boolean в виде строки "true" или "false"
            val isBookPage = when (result) {
                "true" -> true
                else -> false
            }
            callback(isBookPage)
        }
    }

    /**
     * Парсит ссылки на аудиофайлы (будет реализовано позже)
     */
    fun parseAudioUrls(html: String): List<String> {
        // TODO: Реализовать парсинг для каждого сайта
        return emptyList()
    }
}