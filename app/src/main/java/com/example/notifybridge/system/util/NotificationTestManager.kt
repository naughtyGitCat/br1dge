package com.example.notifybridge.system.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTestManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun sendLocalTestNotification(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java) ?: return false
        val channelId = "notifybridge_test"
        manager.createNotificationChannel(
            NotificationChannel(channelId, "NotifyBridge Test", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("NotifyBridge Test")
            .setContentText("这是一条本地测试通知，用于验证监听链路。")
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
        return true
    }
}
