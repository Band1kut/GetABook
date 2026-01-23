package com.head2head.getabook.data.datasource

import android.content.Context
import android.util.Log
import com.head2head.getabook.domain.model.AudioBookSite
import org.json.JSONArray
import org.json.JSONObject

class SitesLocalDataSource(
    private val context: Context
) {

    fun loadSites(): List<AudioBookSite> {
        Log.d("SitesLocalDataSource", "loadSites() called")

        return try {
            val json = loadJsonFromAssets("sites.json")
            Log.d("SitesLocalDataSource", "JSON loaded: $json")

            val sites = parseSites(json)
            Log.d("SitesLocalDataSource", "Parsed sites: $sites")

            sites
        } catch (e: Exception) {
            Log.e("SitesLocalDataSource", "Error loading sites", e)
            emptyList()
        }
    }

    private fun loadJsonFromAssets(fileName: String): String {
        Log.d("SitesLocalDataSource", "Opening asset: $fileName")

        return context.assets.open(fileName).bufferedReader().use {
            val text = it.readText()
            Log.d("SitesLocalDataSource", "File read successfully, length=${text.length}")
            text
        }
    }

    private fun parseSites(json: String): List<AudioBookSite> {
        Log.d("SitesLocalDataSource", "parseSites() called")

        val result = mutableListOf<AudioBookSite>()

        // --- Format 1: Array ---
        try {
            val jsonArray = JSONArray(json)
            Log.d("SitesLocalDataSource", "JSON is array, size=${jsonArray.length()}")

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    AudioBookSite(
                        domain = obj.getString("domain"),
                        bookPattern = obj.getString("bookPattern"),
                        hideElement = obj.getJSONArray("hideElement").let {
                            arr -> List(arr.length()) { arr.getString(it) }
                        }
                    )
                )
            }

            return result
        } catch (_: Exception) {
            Log.w("SitesLocalDataSource", "JSON is not array, trying object format…")
        }

        // --- Format 2: Object ---
        try {
            val jsonObject = JSONObject(json)
            Log.d("SitesLocalDataSource", "JSON is object, keys=${jsonObject.length()}")

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val domain = keys.next()
                val obj = jsonObject.getJSONObject(domain)

                result.add(
                    AudioBookSite(
                        domain = domain,
                        bookPattern = obj.getString("book_pattern"),
                        hideElement = obj.getJSONArray("hideElement").let {
                                arr -> List(arr.length()) { arr.getString(it) }
                        }
                    )
                )
            }

            return result
        } catch (e: Exception) {
            Log.e("SitesLocalDataSource", "Failed to parse JSON", e)
        }

        return emptyList()
    }
}
