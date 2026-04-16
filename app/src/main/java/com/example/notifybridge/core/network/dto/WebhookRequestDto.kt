package com.example.notifybridge.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class WebhookRequestDto(
    val appPackage: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val postTime: Long,
    val receivedAt: Long,
    val deviceModel: String,
    val androidVersion: String,
)
