package com.example.notifybridge.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BarkPushRequestDto(
    val title: String,
    val body: String,
    val subtitle: String? = null,
    @SerialName("device_key") val deviceKey: String,
    val group: String? = null,
    val url: String? = null,
    val level: String = "active",
)
