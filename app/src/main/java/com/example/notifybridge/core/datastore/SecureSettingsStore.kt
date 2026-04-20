package com.example.notifybridge.core.datastore

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class SecureSettingsSnapshot(
    val barkDeviceKey: String = "",
    val barkDeviceKeys: List<String> = emptyList(),
    val telegramBotToken: String = "",
    val slackWebhookUrl: String = "",
    val emailUsername: String = "",
    val emailPassword: String = "",
)

@Singleton
class SecureSettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private object Keys {
        const val barkDeviceKey = "secure_bark_device_key"
        const val barkDeviceKeys = "secure_bark_device_keys"
        const val telegramBotToken = "secure_telegram_bot_token"
        const val slackWebhookUrl = "secure_slack_webhook_url"
        const val emailUsername = "secure_email_username"
        const val emailPassword = "secure_email_password"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "notifybridge_secure_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun snapshot(): SecureSettingsSnapshot {
        return SecureSettingsSnapshot(
            barkDeviceKey = preferences.getString(Keys.barkDeviceKey, "").orEmpty(),
            barkDeviceKeys = preferences.getString(Keys.barkDeviceKeys, "").toTokenList(),
            telegramBotToken = preferences.getString(Keys.telegramBotToken, "").orEmpty(),
            slackWebhookUrl = preferences.getString(Keys.slackWebhookUrl, "").orEmpty(),
            emailUsername = preferences.getString(Keys.emailUsername, "").orEmpty(),
            emailPassword = preferences.getString(Keys.emailPassword, "").orEmpty(),
        )
    }

    fun update(snapshot: SecureSettingsSnapshot) {
        preferences.edit {
            putString(Keys.barkDeviceKey, snapshot.barkDeviceKey)
            putString(Keys.barkDeviceKeys, snapshot.barkDeviceKeys.joinToString(","))
            putString(Keys.telegramBotToken, snapshot.telegramBotToken)
            putString(Keys.slackWebhookUrl, snapshot.slackWebhookUrl)
            putString(Keys.emailUsername, snapshot.emailUsername)
            putString(Keys.emailPassword, snapshot.emailPassword)
        }
    }
}

private fun String?.toTokenList(): List<String> = this
    .orEmpty()
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
