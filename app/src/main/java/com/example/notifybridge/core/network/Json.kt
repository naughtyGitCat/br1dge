package com.example.notifybridge.core.network

import com.example.notifybridge.domain.model.ForwardPayload
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val appJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    prettyPrint = true
}

@kotlinx.serialization.Serializable
private data class SerializableForwardPayload(
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

fun ForwardPayload.toJson(): String = appJson.encodeToString(
    SerializableForwardPayload(
        appPackage = appPackage,
        appName = appName,
        title = title,
        text = text,
        subText = subText,
        postTime = postTime,
        receivedAt = receivedAt,
        deviceModel = deviceModel,
        androidVersion = androidVersion,
    )
)
