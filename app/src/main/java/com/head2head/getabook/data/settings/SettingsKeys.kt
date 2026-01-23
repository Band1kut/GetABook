package com.head2head.getabook.data.settings

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object SettingsKeys {
    val SEARCH_ENGINE = stringPreferencesKey("search_engine")
    val DOWNLOAD_FOLDER = stringPreferencesKey("download_folder")
    val ENABLED_SITES = stringSetPreferencesKey("enabled_sites")
}
