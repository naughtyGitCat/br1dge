package com.example.notifybridge.system.util

import android.content.Context
import android.net.Uri
import com.example.notifybridge.core.network.appJson
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.BarkGroupMode
import com.example.notifybridge.domain.model.DeliveryChannel
import com.example.notifybridge.domain.model.EmailSecurityMode
import com.example.notifybridge.domain.model.FilterRuleSet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class SettingsBackupDto(
    val schemaVersion: Int = 1,
    val exportedAt: Long,
    val settings: AppSettingsDto,
)

@Serializable
private data class AppSettingsDto(
    val prominentDisclosureAccepted: Boolean,
    val forwardingEnabled: Boolean,
    val cancelNotificationOnSuccess: Boolean,
    val preventChannelLoop: Boolean,
    val deliveryChannel: DeliveryChannel,
    val barkServerUrl: String,
    val barkDeviceKey: String,
    val barkDeviceKeys: List<String>,
    val barkLevel: String,
    val barkVolume: Int?,
    val barkBadge: Int?,
    val barkCall: Boolean,
    val barkAutoCopy: Boolean,
    val barkCopy: String,
    val barkSound: String,
    val barkIcon: String,
    val barkImage: String,
    val barkGroupMode: BarkGroupMode,
    val barkGroupCustom: String,
    val barkCiphertext: String,
    val barkIsArchive: Boolean?,
    val barkUrl: String,
    val barkAction: String,
    val barkNotificationId: String,
    val barkDelete: Boolean,
    val barkUseMarkdown: Boolean,
    val telegramBotToken: String,
    val telegramChatId: String,
    val telegramMessageThreadId: String,
    val telegramDisableNotification: Boolean,
    val telegramUseMarkdown: Boolean,
    val slackWebhookUrl: String,
    val slackUsername: String,
    val slackIconEmoji: String,
    val emailSmtpHost: String,
    val emailSmtpPort: Int,
    val emailSecurityMode: EmailSecurityMode,
    val emailUsername: String,
    val emailPassword: String,
    val emailFromAddress: String,
    val emailToAddress: String,
    val emailSubjectPrefix: String,
    val filterRuleSet: FilterRuleSetDto,
    val connectTimeoutSeconds: Int,
    val readTimeoutSeconds: Int,
)

@Serializable
private data class FilterRuleSetDto(
    val enabled: Boolean,
    val allowedPackages: Set<String>,
    val blockedPackages: Set<String>,
    val keywordWhitelist: Set<String>,
    val keywordBlacklist: Set<String>,
    val excludeSystemNotifications: Boolean,
    val excludeOngoingNotifications: Boolean,
    val excludeEmptyTextNotifications: Boolean,
    val dedupeWindowSeconds: Int,
    val autoRetryEnabled: Boolean,
)

@Singleton
class SettingsBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun encode(settings: AppSettings): String {
        val backup = SettingsBackupDto(
            exportedAt = System.currentTimeMillis(),
            settings = settings.toDto(),
        )
        return appJson.encodeToString(backup)
    }

    fun decode(json: String): AppSettings {
        val backup = appJson.decodeFromString<SettingsBackupDto>(json)
        require(backup.schemaVersion == 1) { "Unsupported backup schema: ${backup.schemaVersion}" }
        return backup.settings.toDomain()
    }

    @Throws(IOException::class)
    fun exportToUri(uri: Uri, settings: AppSettings) {
        val json = encode(settings)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray())
        } ?: throw IOException("Unable to open export destination")
    }

    @Throws(IOException::class)
    fun importFromUri(uri: Uri): AppSettings {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: throw IOException("Unable to open backup file")
        return decode(json)
    }
}

private fun AppSettings.toDto(): AppSettingsDto = AppSettingsDto(
    prominentDisclosureAccepted = prominentDisclosureAccepted,
    forwardingEnabled = forwardingEnabled,
    cancelNotificationOnSuccess = cancelNotificationOnSuccess,
    preventChannelLoop = preventChannelLoop,
    deliveryChannel = deliveryChannel,
    barkServerUrl = barkServerUrl,
    barkDeviceKey = barkDeviceKey,
    barkDeviceKeys = barkDeviceKeys,
    barkLevel = barkLevel,
    barkVolume = barkVolume,
    barkBadge = barkBadge,
    barkCall = barkCall,
    barkAutoCopy = barkAutoCopy,
    barkCopy = barkCopy,
    barkSound = barkSound,
    barkIcon = barkIcon,
    barkImage = barkImage,
    barkGroupMode = barkGroupMode,
    barkGroupCustom = barkGroupCustom,
    barkCiphertext = barkCiphertext,
    barkIsArchive = barkIsArchive,
    barkUrl = barkUrl,
    barkAction = barkAction,
    barkNotificationId = barkNotificationId,
    barkDelete = barkDelete,
    barkUseMarkdown = barkUseMarkdown,
    telegramBotToken = telegramBotToken,
    telegramChatId = telegramChatId,
    telegramMessageThreadId = telegramMessageThreadId,
    telegramDisableNotification = telegramDisableNotification,
    telegramUseMarkdown = telegramUseMarkdown,
    slackWebhookUrl = slackWebhookUrl,
    slackUsername = slackUsername,
    slackIconEmoji = slackIconEmoji,
    emailSmtpHost = emailSmtpHost,
    emailSmtpPort = emailSmtpPort,
    emailSecurityMode = emailSecurityMode,
    emailUsername = emailUsername,
    emailPassword = emailPassword,
    emailFromAddress = emailFromAddress,
    emailToAddress = emailToAddress,
    emailSubjectPrefix = emailSubjectPrefix,
    filterRuleSet = filterRuleSet.toDto(),
    connectTimeoutSeconds = connectTimeoutSeconds,
    readTimeoutSeconds = readTimeoutSeconds,
)

private fun FilterRuleSet.toDto(): FilterRuleSetDto = FilterRuleSetDto(
    enabled = enabled,
    allowedPackages = allowedPackages,
    blockedPackages = blockedPackages,
    keywordWhitelist = keywordWhitelist,
    keywordBlacklist = keywordBlacklist,
    excludeSystemNotifications = excludeSystemNotifications,
    excludeOngoingNotifications = excludeOngoingNotifications,
    excludeEmptyTextNotifications = excludeEmptyTextNotifications,
    dedupeWindowSeconds = dedupeWindowSeconds,
    autoRetryEnabled = autoRetryEnabled,
)

private fun AppSettingsDto.toDomain(): AppSettings = AppSettings(
    prominentDisclosureAccepted = prominentDisclosureAccepted,
    forwardingEnabled = forwardingEnabled,
    cancelNotificationOnSuccess = cancelNotificationOnSuccess,
    preventChannelLoop = preventChannelLoop,
    deliveryChannel = deliveryChannel,
    barkServerUrl = barkServerUrl,
    barkDeviceKey = barkDeviceKey,
    barkDeviceKeys = barkDeviceKeys,
    barkLevel = barkLevel,
    barkVolume = barkVolume,
    barkBadge = barkBadge,
    barkCall = barkCall,
    barkAutoCopy = barkAutoCopy,
    barkCopy = barkCopy,
    barkSound = barkSound,
    barkIcon = barkIcon,
    barkImage = barkImage,
    barkGroupMode = barkGroupMode,
    barkGroupCustom = barkGroupCustom,
    barkCiphertext = barkCiphertext,
    barkIsArchive = barkIsArchive,
    barkUrl = barkUrl,
    barkAction = barkAction,
    barkNotificationId = barkNotificationId,
    barkDelete = barkDelete,
    barkUseMarkdown = barkUseMarkdown,
    telegramBotToken = telegramBotToken,
    telegramChatId = telegramChatId,
    telegramMessageThreadId = telegramMessageThreadId,
    telegramDisableNotification = telegramDisableNotification,
    telegramUseMarkdown = telegramUseMarkdown,
    slackWebhookUrl = slackWebhookUrl,
    slackUsername = slackUsername,
    slackIconEmoji = slackIconEmoji,
    emailSmtpHost = emailSmtpHost,
    emailSmtpPort = emailSmtpPort,
    emailSecurityMode = emailSecurityMode,
    emailUsername = emailUsername,
    emailPassword = emailPassword,
    emailFromAddress = emailFromAddress,
    emailToAddress = emailToAddress,
    emailSubjectPrefix = emailSubjectPrefix,
    filterRuleSet = filterRuleSet.toDomain(),
    connectTimeoutSeconds = connectTimeoutSeconds,
    readTimeoutSeconds = readTimeoutSeconds,
)

private fun FilterRuleSetDto.toDomain(): FilterRuleSet = FilterRuleSet(
    enabled = enabled,
    allowedPackages = allowedPackages,
    blockedPackages = blockedPackages,
    keywordWhitelist = keywordWhitelist,
    keywordBlacklist = keywordBlacklist,
    excludeSystemNotifications = excludeSystemNotifications,
    excludeOngoingNotifications = excludeOngoingNotifications,
    excludeEmptyTextNotifications = excludeEmptyTextNotifications,
    dedupeWindowSeconds = dedupeWindowSeconds,
    autoRetryEnabled = autoRetryEnabled,
)
