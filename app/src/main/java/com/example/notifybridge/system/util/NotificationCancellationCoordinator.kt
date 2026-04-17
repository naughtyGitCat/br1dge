package com.example.notifybridge.system.util

import android.service.notification.NotificationListenerService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCancellationCoordinator @Inject constructor() {

    @Volatile
    private var activeService: NotificationListenerService? = null

    fun attach(service: NotificationListenerService) {
        activeService = service
    }

    fun detach(service: NotificationListenerService) {
        if (activeService === service) {
            activeService = null
        }
    }

    fun cancelNotification(notificationKey: String): Boolean {
        if (notificationKey.isBlank()) return false
        val service = activeService ?: return false
        return runCatching {
            service.cancelNotification(notificationKey)
            true
        }.getOrDefault(false)
    }
}
