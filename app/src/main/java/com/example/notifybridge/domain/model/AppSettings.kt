package com.example.notifybridge.domain.model

enum class BarkGroupMode {
    APP_NAME,
    DEVICE_NAME,
    APP_NAME_AT_DEVICE_NAME,
    CUSTOM,
}

data class AppSettings(
    val forwardingEnabled: Boolean = false,
    val cancelNotificationOnSuccess: Boolean = false,
    val barkServerUrl: String = "https://api.day.app",
    val barkDeviceKey: String = "",
    val barkDeviceKeys: List<String> = emptyList(),
    val barkLevel: String = "active",
    val barkVolume: Int? = null,
    val barkBadge: Int? = null,
    val barkCall: Boolean = false,
    val barkAutoCopy: Boolean = false,
    val barkCopy: String = "",
    val barkSound: String = "",
    val barkIcon: String = "",
    val barkImage: String = "",
    val barkGroupMode: BarkGroupMode = BarkGroupMode.APP_NAME_AT_DEVICE_NAME,
    val barkGroupCustom: String = "",
    val barkCiphertext: String = "",
    val barkIsArchive: Boolean? = null,
    val barkUrl: String = "",
    val barkAction: String = "",
    val barkNotificationId: String = "",
    val barkDelete: Boolean = false,
    val barkUseMarkdown: Boolean = false,
    val filterRuleSet: FilterRuleSet = FilterRuleSet(),
    val connectTimeoutSeconds: Int = 15,
    val readTimeoutSeconds: Int = 20,
)
