package com.example.notifybridge.domain.model

data class AppSettings(
    val forwardingEnabled: Boolean = false,
    val barkServerUrl: String = "https://api.day.app",
    val barkDeviceKey: String = "",
    val filterRuleSet: FilterRuleSet = FilterRuleSet(),
    val connectTimeoutSeconds: Int = 15,
    val readTimeoutSeconds: Int = 20,
)
