package com.example.notifybridge.domain.model

data class DeliveryRecord(
    val eventId: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val status: DeliveryStatus,
    val attemptCount: Int,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val nextRetryAt: Long?,
    val payloadJson: String,
    val responseCode: Int?,
)
