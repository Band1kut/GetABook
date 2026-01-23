package com.head2head.getabook.data.settings

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")
