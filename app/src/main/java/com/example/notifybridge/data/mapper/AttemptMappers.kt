package com.example.notifybridge.data.mapper

import com.example.notifybridge.core.database.entity.DeliveryAttemptEntity
import com.example.notifybridge.domain.model.DeliveryAttempt

fun DeliveryAttemptEntity.toDomain(): DeliveryAttempt = DeliveryAttempt(
    id = id,
    eventId = eventId,
    payloadJson = payloadJson,
    responseCode = responseCode,
    errorMessage = errorMessage,
    createdAt = createdAt,
)
