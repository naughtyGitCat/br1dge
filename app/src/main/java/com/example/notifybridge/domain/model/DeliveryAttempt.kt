package com.example.notifybridge.domain.model

data class DeliveryAttempt(
    val id: Long,
    val eventId: String,
    val payloadJson: String,
    val responseCode: Int?,
    val errorMessage: String?,
    val createdAt: Long,
)
