package com.example.notifybridge.system.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.example.notifybridge.system.service.BridgeNotificationListenerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAccessManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isNotificationAccessEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
        ).orEmpty()
        val serviceName = ComponentName(context, BridgeNotificationListenerService::class.java).flattenToString()
        return enabledListeners.contains(serviceName)
    }
}
