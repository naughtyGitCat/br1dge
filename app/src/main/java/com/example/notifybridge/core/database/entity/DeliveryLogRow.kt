package com.example.notifybridge.core.database.entity

data class DeliveryLogRow(
    val eventId: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val status: String,
    val attemptCount: Int,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val nextRetryAt: Long?,
    val payloadJson: String,
    val responseCode: Int?,
)
