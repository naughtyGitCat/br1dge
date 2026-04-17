package com.example.notifybridge.data.mapper

import com.example.notifybridge.core.database.entity.NotificationEventEntity
import com.example.notifybridge.core.database.entity.OutboxEntity
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.NotificationEvent

fun NotificationEvent.toEntity(): NotificationEventEntity = NotificationEventEntity(
    eventId = eventId,
    packageName = packageName,
    appName = appName,
    title = title,
    text = text,
    subText = subText,
    postTime = postTime,
    notificationKey = notificationKey,
    ongoing = ongoing,
    clearable = clearable,
    isSystemNotification = isSystemNotification,
    receivedAt = receivedAt,
)

fun NotificationEventEntity.toDomain(): NotificationEvent = NotificationEvent(
    eventId = eventId,
    packageName = packageName,
    appName = appName,
    title = title,
    text = text,
    subText = subText,
    postTime = postTime,
    notificationKey = notificationKey,
    ongoing = ongoing,
    clearable = clearable,
    isSystemNotification = isSystemNotification,
    receivedAt = receivedAt,
)

fun OutboxEntity.toDomain(appName: String, title: String?, text: String?): DeliveryRecord = DeliveryRecord(
    eventId = eventId,
    appName = appName,
    title = title,
    text = text,
    status = status,
    attemptCount = attemptCount,
    errorMessage = errorMessage,
    createdAt = createdAt,
    updatedAt = updatedAt,
    nextRetryAt = nextRetryAt,
    payloadJson = payloadJson,
    responseCode = responseCode,
)
