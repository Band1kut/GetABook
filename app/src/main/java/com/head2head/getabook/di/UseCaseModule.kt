package com.head2head.getabook.di

import com.head2head.getabook.data.active.ActiveSitesRepository
import com.head2head.getabook.domain.repository.SettingsRepository
import com.head2head.getabook.domain.repository.SitesRepository
import com.head2head.getabook.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideBuildSearchUrlUseCase(
        settingsRepository: SettingsRepository,
        activeSitesRepository: ActiveSitesRepository
    ): BuildSearchUrlUseCase =
        BuildSearchUrlUseCase(settingsRepository, activeSitesRepository)

    @Provides
    @Singleton
    fun provideDetectBookPageUseCase(): DetectBookPageUseCase =
        DetectBookPageUseCase()

    @Provides
    @Singleton
    fun provideGetSiteByDomainUseCase(
        sitesRepository: SitesRepository
    ): GetSiteByDomainUseCase =
        GetSiteByDomainUseCase(sitesRepository)

    @Provides
    @Singleton
    fun provideDownloadAudioUseCase(
        downloadManager: com.head2head.getabook.data.download.DownloadManager
    ): DownloadAudioUseCase =
        DownloadAudioUseCase(downloadManager)
}
