package com.head2head.getabook

import android.app.Application
import com.head2head.getabook.data.SitesManager
import android.util.Log

class GetABookApp : Application() {
    private val TAG = "GetABookApp"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Приложение GetABook запущено")

        val startTime = System.currentTimeMillis()

        // Инициализация менеджера сайтов
        SitesManager.initialize(this)

        val initTime = System.currentTimeMillis() - startTime
        Log.i(TAG, "Инициализация приложения завершена за ${initTime}ms")
    }

    override fun onTerminate() {
        Log.i(TAG, "Приложение GetABook завершает работу")
        super.onTerminate()
    }
}