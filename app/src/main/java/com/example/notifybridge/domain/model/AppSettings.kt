package com.example.notifybridge.domain.model

enum class DeliveryChannel {
    BARK,
    TELEGRAM,
    SLACK,
    EMAIL,
}

enum class EmailSecurityMode {
    NONE,
    STARTTLS,
    SSL_TLS,
}

enum class BarkGroupMode {
    APP_NAME,
    DEVICE_NAME,
    APP_NAME_AT_DEVICE_NAME,
    CUSTOM,
}

data class AppSettings(
    val forwardingEnabled: Boolean = false,
    val cancelNotificationOnSuccess: Boolean = false,
    val deliveryChannel: DeliveryChannel = DeliveryChannel.BARK,
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
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val telegramMessageThreadId: String = "",
    val telegramDisableNotification: Boolean = false,
    val telegramUseMarkdown: Boolean = false,
    val slackWebhookUrl: String = "",
    val slackUsername: String = "",
    val slackIconEmoji: String = "",
    val emailSmtpHost: String = "",
    val emailSmtpPort: Int = 587,
    val emailSecurityMode: EmailSecurityMode = EmailSecurityMode.STARTTLS,
    val emailUsername: String = "",
    val emailPassword: String = "",
    val emailFromAddress: String = "",
    val emailToAddress: String = "",
    val emailSubjectPrefix: String = "[NotifyBridge]",
    val filterRuleSet: FilterRuleSet = FilterRuleSet(),
    val connectTimeoutSeconds: Int = 15,
    val readTimeoutSeconds: Int = 20,
)
