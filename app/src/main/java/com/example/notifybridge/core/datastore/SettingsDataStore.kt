package com.example.notifybridge.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.BarkGroupMode
import com.example.notifybridge.domain.model.DeliveryChannel
import com.example.notifybridge.domain.model.EmailSecurityMode
import com.example.notifybridge.domain.model.FilterRuleSet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "notifybridge_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val forwardingEnabled = booleanPreferencesKey("forwarding_enabled")
        val cancelNotificationOnSuccess = booleanPreferencesKey("cancel_notification_on_success")
        val deliveryChannel = stringPreferencesKey("delivery_channel")
        val barkServerUrl = stringPreferencesKey("bark_server_url")
        val barkDeviceKey = stringPreferencesKey("bark_device_key")
        val barkDeviceKeys = stringPreferencesKey("bark_device_keys")
        val barkLevel = stringPreferencesKey("bark_level")
        val barkVolume = intPreferencesKey("bark_volume")
        val barkBadge = intPreferencesKey("bark_badge")
        val barkCall = booleanPreferencesKey("bark_call")
        val barkAutoCopy = booleanPreferencesKey("bark_auto_copy")
        val barkCopy = stringPreferencesKey("bark_copy")
        val barkSound = stringPreferencesKey("bark_sound")
        val barkIcon = stringPreferencesKey("bark_icon")
        val barkImage = stringPreferencesKey("bark_image")
        val barkGroup = stringPreferencesKey("bark_group")
        val barkGroupMode = stringPreferencesKey("bark_group_mode")
        val barkGroupCustom = stringPreferencesKey("bark_group_custom")
        val barkCiphertext = stringPreferencesKey("bark_ciphertext")
        val barkIsArchive = booleanPreferencesKey("bark_is_archive")
        val barkUrl = stringPreferencesKey("bark_url")
        val barkAction = stringPreferencesKey("bark_action")
        val barkNotificationId = stringPreferencesKey("bark_notification_id")
        val barkDelete = booleanPreferencesKey("bark_delete")
        val barkUseMarkdown = booleanPreferencesKey("bark_use_markdown")
        val telegramBotToken = stringPreferencesKey("telegram_bot_token")
        val telegramChatId = stringPreferencesKey("telegram_chat_id")
        val telegramMessageThreadId = stringPreferencesKey("telegram_message_thread_id")
        val telegramDisableNotification = booleanPreferencesKey("telegram_disable_notification")
        val telegramUseMarkdown = booleanPreferencesKey("telegram_use_markdown")
        val slackWebhookUrl = stringPreferencesKey("slack_webhook_url")
        val slackUsername = stringPreferencesKey("slack_username")
        val slackIconEmoji = stringPreferencesKey("slack_icon_emoji")
        val emailSmtpHost = stringPreferencesKey("email_smtp_host")
        val emailSmtpPort = intPreferencesKey("email_smtp_port")
        val emailSecurityMode = stringPreferencesKey("email_security_mode")
        val emailUsername = stringPreferencesKey("email_username")
        val emailPassword = stringPreferencesKey("email_password")
        val emailFromAddress = stringPreferencesKey("email_from_address")
        val emailToAddress = stringPreferencesKey("email_to_address")
        val emailSubjectPrefix = stringPreferencesKey("email_subject_prefix")
        val filtersEnabled = booleanPreferencesKey("filters_enabled")
        val allowedPackages = stringPreferencesKey("allowed_packages")
        val blockedPackages = stringPreferencesKey("blocked_packages")
        val keywordWhitelist = stringPreferencesKey("keyword_whitelist")
        val keywordBlacklist = stringPreferencesKey("keyword_blacklist")
        val excludeSystem = booleanPreferencesKey("exclude_system")
        val excludeOngoing = booleanPreferencesKey("exclude_ongoing")
        val excludeEmpty = booleanPreferencesKey("exclude_empty")
        val dedupeSeconds = intPreferencesKey("dedupe_seconds")
        val autoRetryEnabled = booleanPreferencesKey("auto_retry_enabled")
        val connectTimeoutSeconds = intPreferencesKey("connect_timeout_seconds")
        val readTimeoutSeconds = intPreferencesKey("read_timeout_seconds")
    }

    fun observeSettings(): Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        val legacyGroup = prefs[Keys.barkGroup].orEmpty()
        val barkGroupMode = prefs[Keys.barkGroupMode]
            ?.let { runCatching { BarkGroupMode.valueOf(it) }.getOrNull() }
            ?: if (legacyGroup.isNotBlank()) BarkGroupMode.CUSTOM else BarkGroupMode.APP_NAME_AT_DEVICE_NAME
        AppSettings(
            forwardingEnabled = prefs[Keys.forwardingEnabled] ?: false,
            cancelNotificationOnSuccess = prefs[Keys.cancelNotificationOnSuccess] ?: false,
            deliveryChannel = prefs[Keys.deliveryChannel]
                ?.let { runCatching { DeliveryChannel.valueOf(it) }.getOrNull() }
                ?: DeliveryChannel.BARK,
            barkServerUrl = prefs[Keys.barkServerUrl] ?: "https://api.day.app",
            barkDeviceKey = prefs[Keys.barkDeviceKey].orEmpty(),
            barkDeviceKeys = prefs[Keys.barkDeviceKeys].toTokenList(),
            barkLevel = prefs[Keys.barkLevel] ?: "active",
            barkVolume = prefs[Keys.barkVolume],
            barkBadge = prefs[Keys.barkBadge],
            barkCall = prefs[Keys.barkCall] ?: false,
            barkAutoCopy = prefs[Keys.barkAutoCopy] ?: false,
            barkCopy = prefs[Keys.barkCopy].orEmpty(),
            barkSound = prefs[Keys.barkSound].orEmpty(),
            barkIcon = prefs[Keys.barkIcon].orEmpty(),
            barkImage = prefs[Keys.barkImage].orEmpty(),
            barkGroupMode = barkGroupMode,
            barkGroupCustom = prefs[Keys.barkGroupCustom] ?: legacyGroup,
            barkCiphertext = prefs[Keys.barkCiphertext].orEmpty(),
            barkIsArchive = prefs[Keys.barkIsArchive],
            barkUrl = prefs[Keys.barkUrl].orEmpty(),
            barkAction = prefs[Keys.barkAction].orEmpty(),
            barkNotificationId = prefs[Keys.barkNotificationId].orEmpty(),
            barkDelete = prefs[Keys.barkDelete] ?: false,
            barkUseMarkdown = prefs[Keys.barkUseMarkdown] ?: false,
            telegramBotToken = prefs[Keys.telegramBotToken].orEmpty(),
            telegramChatId = prefs[Keys.telegramChatId].orEmpty(),
            telegramMessageThreadId = prefs[Keys.telegramMessageThreadId].orEmpty(),
            telegramDisableNotification = prefs[Keys.telegramDisableNotification] ?: false,
            telegramUseMarkdown = prefs[Keys.telegramUseMarkdown] ?: false,
            slackWebhookUrl = prefs[Keys.slackWebhookUrl].orEmpty(),
            slackUsername = prefs[Keys.slackUsername].orEmpty(),
            slackIconEmoji = prefs[Keys.slackIconEmoji].orEmpty(),
            emailSmtpHost = prefs[Keys.emailSmtpHost].orEmpty(),
            emailSmtpPort = prefs[Keys.emailSmtpPort] ?: 587,
            emailSecurityMode = prefs[Keys.emailSecurityMode]
                ?.let { runCatching { EmailSecurityMode.valueOf(it) }.getOrNull() }
                ?: EmailSecurityMode.STARTTLS,
            emailUsername = prefs[Keys.emailUsername].orEmpty(),
            emailPassword = prefs[Keys.emailPassword].orEmpty(),
            emailFromAddress = prefs[Keys.emailFromAddress].orEmpty(),
            emailToAddress = prefs[Keys.emailToAddress].orEmpty(),
            emailSubjectPrefix = prefs[Keys.emailSubjectPrefix] ?: "[NotifyBridge]",
            filterRuleSet = FilterRuleSet(
                enabled = prefs[Keys.filtersEnabled] ?: false,
                allowedPackages = prefs[Keys.allowedPackages].toTokenSet(),
                blockedPackages = prefs[Keys.blockedPackages].toTokenSet(),
                keywordWhitelist = prefs[Keys.keywordWhitelist].toTokenSet(),
                keywordBlacklist = prefs[Keys.keywordBlacklist].toTokenSet(),
                excludeSystemNotifications = prefs[Keys.excludeSystem] ?: true,
                excludeOngoingNotifications = prefs[Keys.excludeOngoing] ?: false,
                excludeEmptyTextNotifications = prefs[Keys.excludeEmpty] ?: false,
                dedupeWindowSeconds = prefs[Keys.dedupeSeconds] ?: 10,
                autoRetryEnabled = prefs[Keys.autoRetryEnabled] ?: true,
            ),
            connectTimeoutSeconds = prefs[Keys.connectTimeoutSeconds] ?: 15,
            readTimeoutSeconds = prefs[Keys.readTimeoutSeconds] ?: 20,
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.forwardingEnabled] = settings.forwardingEnabled
            prefs[Keys.cancelNotificationOnSuccess] = settings.cancelNotificationOnSuccess
            prefs[Keys.deliveryChannel] = settings.deliveryChannel.name
            prefs[Keys.barkServerUrl] = settings.barkServerUrl
            prefs[Keys.barkDeviceKey] = settings.barkDeviceKey
            prefs[Keys.barkDeviceKeys] = settings.barkDeviceKeys.joinToString(",")
            prefs[Keys.barkLevel] = settings.barkLevel
            settings.barkVolume?.let { prefs[Keys.barkVolume] = it } ?: prefs.remove(Keys.barkVolume)
            settings.barkBadge?.let { prefs[Keys.barkBadge] = it } ?: prefs.remove(Keys.barkBadge)
            prefs[Keys.barkCall] = settings.barkCall
            prefs[Keys.barkAutoCopy] = settings.barkAutoCopy
            prefs[Keys.barkCopy] = settings.barkCopy
            prefs[Keys.barkSound] = settings.barkSound
            prefs[Keys.barkIcon] = settings.barkIcon
            prefs[Keys.barkImage] = settings.barkImage
            prefs[Keys.barkGroupMode] = settings.barkGroupMode.name
            prefs[Keys.barkGroupCustom] = settings.barkGroupCustom
            prefs[Keys.barkGroup] = settings.barkGroupCustom
            prefs[Keys.barkCiphertext] = settings.barkCiphertext
            settings.barkIsArchive?.let { prefs[Keys.barkIsArchive] = it } ?: prefs.remove(Keys.barkIsArchive)
            prefs[Keys.barkUrl] = settings.barkUrl
            prefs[Keys.barkAction] = settings.barkAction
            prefs[Keys.barkNotificationId] = settings.barkNotificationId
            prefs[Keys.barkDelete] = settings.barkDelete
            prefs[Keys.barkUseMarkdown] = settings.barkUseMarkdown
            prefs[Keys.telegramBotToken] = settings.telegramBotToken
            prefs[Keys.telegramChatId] = settings.telegramChatId
            prefs[Keys.telegramMessageThreadId] = settings.telegramMessageThreadId
            prefs[Keys.telegramDisableNotification] = settings.telegramDisableNotification
            prefs[Keys.telegramUseMarkdown] = settings.telegramUseMarkdown
            prefs[Keys.slackWebhookUrl] = settings.slackWebhookUrl
            prefs[Keys.slackUsername] = settings.slackUsername
            prefs[Keys.slackIconEmoji] = settings.slackIconEmoji
            prefs[Keys.emailSmtpHost] = settings.emailSmtpHost
            prefs[Keys.emailSmtpPort] = settings.emailSmtpPort
            prefs[Keys.emailSecurityMode] = settings.emailSecurityMode.name
            prefs[Keys.emailUsername] = settings.emailUsername
            prefs[Keys.emailPassword] = settings.emailPassword
            prefs[Keys.emailFromAddress] = settings.emailFromAddress
            prefs[Keys.emailToAddress] = settings.emailToAddress
            prefs[Keys.emailSubjectPrefix] = settings.emailSubjectPrefix
            prefs[Keys.filtersEnabled] = settings.filterRuleSet.enabled
            prefs[Keys.allowedPackages] = settings.filterRuleSet.allowedPackages.joinToString(",")
            prefs[Keys.blockedPackages] = settings.filterRuleSet.blockedPackages.joinToString(",")
            prefs[Keys.keywordWhitelist] = settings.filterRuleSet.keywordWhitelist.joinToString(",")
            prefs[Keys.keywordBlacklist] = settings.filterRuleSet.keywordBlacklist.joinToString(",")
            prefs[Keys.excludeSystem] = settings.filterRuleSet.excludeSystemNotifications
            prefs[Keys.excludeOngoing] = settings.filterRuleSet.excludeOngoingNotifications
            prefs[Keys.excludeEmpty] = settings.filterRuleSet.excludeEmptyTextNotifications
            prefs[Keys.dedupeSeconds] = settings.filterRuleSet.dedupeWindowSeconds
            prefs[Keys.autoRetryEnabled] = settings.filterRuleSet.autoRetryEnabled
            prefs[Keys.connectTimeoutSeconds] = settings.connectTimeoutSeconds
            prefs[Keys.readTimeoutSeconds] = settings.readTimeoutSeconds
        }
    }
}

private fun String?.toTokenSet(): Set<String> = this
    .orEmpty()
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .toSet()

private fun String?.toTokenList(): List<String> = this
    .orEmpty()
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
