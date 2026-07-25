package com.devcode940.web.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.devcode940.web.data.TabRepository
import com.devcode940.web.domain.LoadUrlUseCase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideTabRepository(): TabRepository = TabRepository()

    @Provides
    fun provideLoadUrlUseCase(): LoadUrlUseCase = LoadUrlUseCase()
}