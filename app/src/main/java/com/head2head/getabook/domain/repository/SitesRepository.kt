package com.head2head.getabook.domain.repository

import com.head2head.getabook.domain.model.AudioBookSite

interface SitesRepository {
    fun getAllSites(): List<AudioBookSite>
    fun getSiteByUrl(url: String): AudioBookSite?
}
