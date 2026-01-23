package com.head2head.getabook

import android.app.Application
import android.util.Log
import com.head2head.getabook.domain.repository.SitesRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GetABookApp : Application() {

    @Inject
    lateinit var sitesRepository: SitesRepository

    override fun onCreate() {
        super.onCreate()

        Log.d("GetABookApp", "Application started, warming up sites…")

        val sites = sitesRepository.getAllSites()

        Log.d("GetABookApp", "Sites warmed up: $sites")
    }
}
