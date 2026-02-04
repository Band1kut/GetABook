package com.head2head.getabook.data.repository

import com.head2head.getabook.data.datasource.SitesLocalDataSource
import com.head2head.getabook.domain.model.AudioBookSite
import com.head2head.getabook.domain.model.AdBlockDto
import com.head2head.getabook.domain.repository.SitesRepository

class SitesRepositoryImpl(
    private val localDataSource: SitesLocalDataSource
) : SitesRepository {

    private val sites: List<AudioBookSite> by lazy {
        localDataSource.loadSites()
    }

//    private val adbHosts: AdBlockDto by lazy {
//        localDataSource.loadAdBlock()
//    }

    override fun getAllSites(): List<AudioBookSite> = sites

    override fun getSiteByUrl(url: String): AudioBookSite? {
        return sites.firstOrNull { site ->
            url.contains(site.domain, ignoreCase = true)
        }
    }
}
