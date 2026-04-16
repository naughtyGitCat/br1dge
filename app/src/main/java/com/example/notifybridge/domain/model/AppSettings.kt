package com.example.notifybridge.domain.model

data class AppSettings(
    val forwardingEnabled: Boolean = false,
    val webhookUrl: String = "",
    val bearerToken: String = "",
    val filterRuleSet: FilterRuleSet = FilterRuleSet(),
    val connectTimeoutSeconds: Int = 15,
    val readTimeoutSeconds: Int = 20,
)
