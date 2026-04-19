package com.example.notifybridge.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TelegramSendMessageDto(
    @SerialName("chat_id") val chatId: String,
    val text: String,
    @SerialName("message_thread_id") val messageThreadId: Int? = null,
    @SerialName("disable_notification") val disableNotification: Boolean = false,
    @SerialName("parse_mode") val parseMode: String? = null,
)
