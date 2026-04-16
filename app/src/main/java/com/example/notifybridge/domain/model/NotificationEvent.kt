package com.example.notifybridge.domain.model

data class NotificationEvent(
    val eventId: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val postTime: Long,
    val notificationKey: String,
    val ongoing: Boolean,
    val clearable: Boolean,
    val isSystemNotification: Boolean,
    val receivedAt: Long,
)
