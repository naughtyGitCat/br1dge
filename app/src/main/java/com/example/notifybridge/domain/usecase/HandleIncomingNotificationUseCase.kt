package com.example.notifybridge.domain.usecase

import com.example.notifybridge.domain.model.NotificationEvent
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import javax.inject.Inject

sealed class HandleIncomingResult {
    data object Enqueued : HandleIncomingResult()
    data class Ignored(val reason: String) : HandleIncomingResult()
}

class HandleIncomingNotificationUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val deliveryLogRepository: DeliveryLogRepository,
    private val shouldForwardNotificationUseCase: ShouldForwardNotificationUseCase,
    private val enqueueNotificationForDeliveryUseCase: EnqueueNotificationForDeliveryUseCase,
) {
    suspend operator fun invoke(event: NotificationEvent): HandleIncomingResult {
        val settings = settingsRepository.getSettings()
        val lastAcceptedAt = deliveryLogRepository.getLastAcceptedAtForDedupe(
            packageName = event.packageName,
            title = event.title,
            text = event.text,
        )
        return when (val decision = shouldForwardNotificationUseCase(event, settings, lastAcceptedAt)) {
            is FilterDecision.Allowed -> {
                enqueueNotificationForDeliveryUseCase(event)
                HandleIncomingResult.Enqueued
            }
            is FilterDecision.Blocked -> HandleIncomingResult.Ignored(decision.reason)
        }
    }
}
