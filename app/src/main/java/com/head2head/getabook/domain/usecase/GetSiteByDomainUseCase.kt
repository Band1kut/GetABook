package com.head2head.getabook.domain.usecase

import com.head2head.getabook.domain.model.AudioBookSite
import com.head2head.getabook.domain.repository.SitesRepository

/**
 * Use-case для поиска сайта по домену.
 *
 * Это первый шаг к тому, чтобы полностью заменить SitesManager.
 */
class GetSiteByDomainUseCase(
    private val sitesRepository: SitesRepository
) {

    operator fun invoke(domain: String): AudioBookSite? {
        if (domain.isBlank()) return null
        return sitesRepository.getSiteByUrl(domain)
    }
}
