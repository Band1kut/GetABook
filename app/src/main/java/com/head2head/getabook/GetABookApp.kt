package com.head2head.getabook

import android.app.Application
import com.head2head.getabook.data.SitesManager

class GetABookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация при запуске приложения
        SitesManager.initialize(this)
    }
}