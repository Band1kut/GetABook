package com.head2head.getabook.data.datasource

import android.content.Context
import com.head2head.getabook.domain.model.AdBlockDto
import org.json.JSONObject
import java.io.IOException

class AdBlockLocalDataSource(
    private val context: Context
) {

    fun loadAdBlockConfig(): AdBlockDto {
        return try {
            val jsonString = readAssetFile("adblock.json")
            val json = JSONObject(jsonString)

            val hostsJsonArray = json.optJSONArray("blockedHosts")
            val hostsList = mutableListOf<String>()

            if (hostsJsonArray != null) {
                for (i in 0 until hostsJsonArray.length()) {
                    hostsList.add(hostsJsonArray.optString(i))
                }
            }

            AdBlockDto(blockedHosts = hostsList)

        } catch (e: Exception) {
            // В случае ошибки возвращаем пустой список, чтобы не ломать приложение
            AdBlockDto(emptyList())
        }
    }

    private fun readAssetFile(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            ""
        }
    }
}
