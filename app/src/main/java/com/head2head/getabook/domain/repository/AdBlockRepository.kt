package com.head2head.getabook.domain.repository

interface AdBlockRepository {
    fun getBlockedHosts(): Set<String>
}
