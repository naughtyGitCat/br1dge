package com.example.notifybridge.data.repository

import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDeliveryLogRepository(impl: DeliveryLogRepositoryImpl): DeliveryLogRepository

    @Binds
    @Singleton
    abstract fun bindForwardRepository(impl: ForwardRepositoryImpl): ForwardRepository
}
