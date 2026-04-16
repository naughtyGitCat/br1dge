package com.example.notifybridge.domain.usecase

import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(settings: AppSettings) {
        settingsRepository.updateSettings { settings }
    }
}
