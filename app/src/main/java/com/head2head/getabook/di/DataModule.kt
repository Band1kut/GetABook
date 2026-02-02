package com.head2head.getabook.di

import android.content.Context
import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.data.datasource.SitesLocalDataSource
import com.head2head.getabook.data.datasource.AdBlockLocalDataSource
import com.head2head.getabook.data.repository.SitesRepositoryImpl
import com.head2head.getabook.data.repository.AdBlockRepositoryImpl
import com.head2head.getabook.domain.repository.SettingsRepository
import com.head2head.getabook.domain.repository.SitesRepository
import com.head2head.getabook.domain.repository.AdBlockRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    // -----------------------------
    // Sites
    // -----------------------------

    @Provides
    @Singleton
    fun provideSitesLocalDataSource(
        @ApplicationContext context: Context
    ): SitesLocalDataSource = SitesLocalDataSource(context)

    @Provides
    @Singleton
    fun provideSitesRepository(
        localDataSource: SitesLocalDataSource
    ): SitesRepository = SitesRepositoryImpl(localDataSource)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
        sitesRepository: SitesRepository
    ): SettingsRepository = SettingsRepository(context, sitesRepository)

    @Provides
    @Singleton
    fun provideActiveSitesRepository(
        sitesRepository: SitesRepository,
        settingsRepository: SettingsRepository
    ): ActiveSitesRepository = ActiveSitesRepository(sitesRepository, settingsRepository)


    // -----------------------------
    // AdBlock
    // -----------------------------

    @Provides
    @Singleton
    fun provideAdBlockLocalDataSource(
        @ApplicationContext context: Context
    ): AdBlockLocalDataSource = AdBlockLocalDataSource(context)

    @Provides
    @Singleton
    fun provideAdBlockRepository(
        localDataSource: AdBlockLocalDataSource
    ): AdBlockRepository = AdBlockRepositoryImpl(localDataSource)

    @Provides
    @Singleton
    fun provideBlockedHosts(
        adBlockRepository: AdBlockRepository
    ): Set<String> = adBlockRepository.getBlockedHosts()
}
