package com.head2head.getabook.data.repository

import com.head2head.getabook.data.datasource.AdBlockLocalDataSource
import com.head2head.getabook.domain.repository.AdBlockRepository

class AdBlockRepositoryImpl(
    private val localDataSource: AdBlockLocalDataSource
) : AdBlockRepository {

    private val cachedHosts: Set<String> by lazy {
        val dto = localDataSource.loadAdBlockConfig()
        dto.blockedHosts
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    override fun getBlockedHosts(): Set<String> = cachedHosts
}
