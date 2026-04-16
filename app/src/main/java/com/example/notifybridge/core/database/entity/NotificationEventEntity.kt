package com.example.notifybridge.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_events")
data class NotificationEventEntity(
    @PrimaryKey val eventId: String,
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
