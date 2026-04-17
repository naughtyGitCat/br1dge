package com.example.notifybridge.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.notifybridge.domain.model.AppSettings
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
        val barkServerUrl = stringPreferencesKey("bark_server_url")
        val barkDeviceKey = stringPreferencesKey("bark_device_key")
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
        AppSettings(
            forwardingEnabled = prefs[Keys.forwardingEnabled] ?: false,
            barkServerUrl = prefs[Keys.barkServerUrl] ?: "https://api.day.app",
            barkDeviceKey = prefs[Keys.barkDeviceKey].orEmpty(),
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
            prefs[Keys.barkServerUrl] = settings.barkServerUrl
            prefs[Keys.barkDeviceKey] = settings.barkDeviceKey
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
