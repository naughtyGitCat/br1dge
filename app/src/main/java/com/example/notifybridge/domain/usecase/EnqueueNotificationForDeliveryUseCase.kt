package com.example.notifybridge.domain.usecase

import com.example.notifybridge.domain.model.NotificationEvent
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import javax.inject.Inject

class EnqueueNotificationForDeliveryUseCase @Inject constructor(
    private val deliveryLogRepository: DeliveryLogRepository,
) {
    suspend operator fun invoke(event: NotificationEvent): Long {
        return deliveryLogRepository.enqueueNotification(event)
    }
}
