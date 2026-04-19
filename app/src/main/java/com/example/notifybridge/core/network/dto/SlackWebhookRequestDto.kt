package com.example.notifybridge.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SlackWebhookRequestDto(
    val text: String,
    val username: String? = null,
    @SerialName("icon_emoji") val iconEmoji: String? = null,
)
