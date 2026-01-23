package com.head2head.getabook.di

import com.head2head.getabook.data.download.DownloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDownloadManager(): DownloadManager = DownloadManager()
}
