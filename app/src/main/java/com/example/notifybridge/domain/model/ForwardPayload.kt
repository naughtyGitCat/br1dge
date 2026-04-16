package com.example.notifybridge.domain.model

data class ForwardPayload(
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
