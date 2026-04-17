package com.example.notifybridge.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BarkPushRequestDto(
    val title: String? = null,
    val body: String? = null,
    val subtitle: String? = null,
    val markdown: String? = null,
    @SerialName("device_key") val deviceKey: String? = null,
    @SerialName("device_keys") val deviceKeys: List<String>? = null,
    val group: String? = null,
    val url: String? = null,
    val level: String = "active",
    val volume: Int? = null,
    val badge: Int? = null,
    val call: String? = null,
    val autoCopy: String? = null,
    val copy: String? = null,
    val sound: String? = null,
    val icon: String? = null,
    val image: String? = null,
    val ciphertext: String? = null,
    val isArchive: String? = null,
    val action: String? = null,
    val id: String? = null,
    val delete: String? = null,
)
