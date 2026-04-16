package com.example.notifybridge.system.util

import android.app.Notification
import android.content.Context
import android.content.pm.ApplicationInfo
import android.service.notification.StatusBarNotification
import com.example.notifybridge.domain.model.NotificationEvent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationParser @Inject constructor() {

    fun parse(context: Context, sbn: StatusBarNotification): NotificationEvent {
        val notification = sbn.notification
        val extras = notification.extras
        val appLabel = resolveAppName(context, sbn.packageName)
        return NotificationEvent(
            eventId = UUID.randomUUID().toString(),
            packageName = sbn.packageName,
            appName = appLabel,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
            postTime = sbn.postTime,
            notificationKey = sbn.key.orEmpty(),
            ongoing = sbn.isOngoing,
            clearable = sbn.isClearable,
            isSystemNotification = isSystemPackage(context, sbn.packageName),
            receivedAt = System.currentTimeMillis(),
        )
    }

    private fun resolveAppName(context: Context, packageName: String): String {
        return runCatching {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    private fun isSystemPackage(context: Context, packageName: String): Boolean {
        return runCatching {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.getOrDefault(false)
    }
}
