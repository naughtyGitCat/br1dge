package com.example.notifybridge.data.repository

import com.example.notifybridge.core.datastore.SettingsDataStore
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = dataStore.observeSettings()

    override suspend fun getSettings(): AppSettings = observeSettings().first()

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = getSettings()
        dataStore.updateSettings(transform(current))
    }
}
