package com.example.notifybridge.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import uk.ngcat.notifybridge.R
import com.example.notifybridge.core.common.StringsProvider
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.BarkGroupMode
import com.example.notifybridge.domain.model.DeliveryChannel
import com.example.notifybridge.domain.model.EmailSecurityMode
import com.example.notifybridge.domain.model.FilterRuleSet
import com.example.notifybridge.domain.model.ForwardResult
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import com.example.notifybridge.domain.usecase.UpdateSettingsUseCase
import com.example.notifybridge.system.util.DebugExportManager
import com.example.notifybridge.system.util.InstalledAppInfo
import com.example.notifybridge.system.util.InstalledAppsProvider
import com.example.notifybridge.system.util.SettingsBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import javax.inject.Inject

sealed class SettingsAction {
    data class Save(val draft: SettingsDraft) : SettingsAction()
    data class TestSend(val draft: SettingsDraft) : SettingsAction()
    data class ExportSettingsBackup(val uri: Uri) : SettingsAction()
    data class ImportSettingsBackup(val uri: Uri) : SettingsAction()
    data object ClearLogs : SettingsAction()
    data object ExportDebug : SettingsAction()
}

data class SettingsDraft(
    val forwardingEnabled: Boolean,
    val cancelNotificationOnSuccess: Boolean,
    val preventChannelLoop: Boolean,
    val deliveryChannel: DeliveryChannel,
    val barkServerUrl: String,
    val barkDeviceKey: String,
    val barkDeviceKeys: String,
    val barkLevel: String,
    val barkVolume: String,
    val barkBadge: String,
    val barkCall: Boolean,
    val barkAutoCopy: Boolean,
    val barkCopy: String,
    val barkSound: String,
    val barkIcon: String,
    val barkImage: String,
    val barkGroupMode: BarkGroupMode,
    val barkGroupCustom: String,
    val barkCiphertext: String,
    val barkIsArchive: Boolean,
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
    val emailSmtpPort: String,
    val emailSecurityMode: EmailSecurityMode,
    val emailUsername: String,
    val emailPassword: String,
    val emailFromAddress: String,
    val emailToAddress: String,
    val emailSubjectPrefix: String,
    val allowedPackages: String,
    val blockedPackages: String,
    val keywordWhitelist: String,
    val keywordBlacklist: String,
    val dedupeSeconds: String,
    val filtersEnabled: Boolean,
    val excludeSystem: Boolean,
    val excludeOngoing: Boolean,
    val excludeEmpty: Boolean,
    val autoRetry: Boolean,
)

data class SettingsValidationState(
    val barkServerUrlError: String? = null,
    val barkDeviceKeyError: String? = null,
    val barkLevelError: String? = null,
    val barkVolumeError: String? = null,
    val barkBadgeError: String? = null,
    val barkActionError: String? = null,
    val barkUrlError: String? = null,
    val barkIconError: String? = null,
    val barkImageError: String? = null,
    val telegramBotTokenError: String? = null,
    val telegramChatIdError: String? = null,
    val slackWebhookUrlError: String? = null,
    val emailSmtpHostError: String? = null,
    val emailSmtpPortError: String? = null,
    val emailUsernameError: String? = null,
    val emailPasswordError: String? = null,
    val emailFromAddressError: String? = null,
    val emailToAddressError: String? = null,
    val dedupeSecondsError: String? = null,
) {
    val isValid: Boolean
        get() = barkServerUrlError == null &&
            barkDeviceKeyError == null &&
            barkLevelError == null &&
            barkVolumeError == null &&
            barkBadgeError == null &&
            barkActionError == null &&
            barkUrlError == null &&
            barkIconError == null &&
            barkImageError == null &&
            telegramBotTokenError == null &&
            telegramChatIdError == null &&
            slackWebhookUrlError == null &&
            emailSmtpHostError == null &&
            emailSmtpPortError == null &&
            emailUsernameError == null &&
            emailPasswordError == null &&
            emailFromAddressError == null &&
            emailToAddressError == null &&
            dedupeSecondsError == null
}

sealed class TestSendState {
    data object Idle : TestSendState()
    data object Running : TestSendState()
    data class Success(val message: String) : TestSendState()
    data class Failure(val message: String) : TestSendState()
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(
        filterRuleSet = FilterRuleSet(enabled = true)
    ),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val message: String? = null,
    val exportingPath: String? = null,
    val validation: SettingsValidationState = SettingsValidationState(),
    val testSendState: TestSendState = TestSendState.Idle,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val stringsProvider: StringsProvider,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val forwardRepository: ForwardRepository,
    private val deliveryLogRepository: DeliveryLogRepository,
    private val debugExportManager: DebugExportManager,
    private val settingsBackupManager: SettingsBackupManager,
    private val installedAppsProvider: InstalledAppsProvider,
) : ViewModel() {

    private val ephemeralState = MutableStateFlow(SettingsUiState())
    private val installedAppsState = MutableStateFlow(emptyList<InstalledAppInfo>())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeSettings(),
        installedAppsState,
        ephemeralState,
    ) { settings, installedApps, ephemeral ->
        SettingsUiState(
            settings = settings,
            installedApps = installedApps,
            message = ephemeral.message,
            exportingPath = ephemeral.exportingPath,
            validation = ephemeral.validation,
            testSendState = ephemeral.testSendState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(settings = AppSettings(filterRuleSet = FilterRuleSet(enabled = true))),
    )

    init {
        viewModelScope.launch {
            installedAppsState.value = withContext(Dispatchers.IO) {
                installedAppsProvider.getInstalledApps()
            }
        }
    }

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.Save -> {
                    val validation = validate(action.draft)
                    if (!validation.isValid) {
                        ephemeralState.value = ephemeralState.value.copy(
                            message = stringsProvider.get(R.string.settings_msg_fix_fields),
                            validation = validation,
                        )
                        return@launch
                    }
                    updateSettingsUseCase(action.draft.toAppSettings())
                    ephemeralState.value = ephemeralState.value.copy(
                        message = stringsProvider.get(R.string.settings_msg_saved),
                        validation = validation,
                    )
                }
                is SettingsAction.TestSend -> {
                    val validation = validate(action.draft)
                    if (!validation.isValid) {
                        ephemeralState.value = ephemeralState.value.copy(
                            message = stringsProvider.get(R.string.settings_msg_fix_fields),
                            validation = validation,
                            testSendState = TestSendState.Failure(stringsProvider.get(R.string.settings_msg_validation_failed)),
                        )
                        return@launch
                    }
                    ephemeralState.value = ephemeralState.value.copy(
                        message = stringsProvider.get(R.string.settings_msg_testing),
                        validation = validation,
                        testSendState = TestSendState.Running,
                    )
                        val message = when (val result = forwardRepository.sendTestPayload(action.draft.toAppSettings())) {
                        is ForwardResult.Success -> {
                            ephemeralState.value = ephemeralState.value.copy(
                                testSendState = TestSendState.Success(stringsProvider.get(R.string.settings_msg_bark_ok)),
                            )
                            stringsProvider.get(R.string.settings_msg_test_success)
                        }
                        is ForwardResult.Failure -> {
                            ephemeralState.value = ephemeralState.value.copy(
                                testSendState = TestSendState.Failure(result.error.message),
                            )
                            stringsProvider.get(R.string.settings_msg_test_failed, result.error.message)
                        }
                    }
                    ephemeralState.value = ephemeralState.value.copy(message = message)
                }
                is SettingsAction.ExportSettingsBackup -> {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            settingsBackupManager.exportToUri(action.uri, settingsRepository.getSettings())
                        }
                    }.onSuccess {
                        ephemeralState.value = ephemeralState.value.copy(
                            message = stringsProvider.get(R.string.settings_msg_backup_exported),
                        )
                    }.onFailure { throwable ->
                        ephemeralState.value = ephemeralState.value.copy(
                            message = stringsProvider.get(R.string.settings_msg_backup_export_failed, throwable.message.orEmpty()),
                        )
                    }
                }
                is SettingsAction.ImportSettingsBackup -> {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            settingsBackupManager.importFromUri(action.uri)
                        }
                    }.onSuccess { imported ->
                        updateSettingsUseCase(imported)
                        ephemeralState.value = ephemeralState.value.copy(
                            message = stringsProvider.get(R.string.settings_msg_backup_imported),
                        )
                    }.onFailure { throwable ->
                        ephemeralState.value = ephemeralState.value.copy(
                            message = stringsProvider.get(R.string.settings_msg_backup_import_failed, throwable.message.orEmpty()),
                        )
                    }
                }
                SettingsAction.ClearLogs -> {
                    deliveryLogRepository.clearLogs()
                    ephemeralState.value = ephemeralState.value.copy(message = stringsProvider.get(R.string.settings_msg_logs_cleared))
                }
                SettingsAction.ExportDebug -> {
                    val file = debugExportManager.exportJson(deliveryLogRepository.exportDebugSnapshot())
                    ephemeralState.value = ephemeralState.value.copy(
                        message = stringsProvider.get(R.string.settings_msg_debug_exported),
                        exportingPath = file.absolutePath,
                    )
                }
            }
        }
    }

    private fun validate(draft: SettingsDraft): SettingsValidationState {
        val barkServerUrl = draft.barkServerUrl.trim()
        val barkServerUrlError = when {
            draft.deliveryChannel != DeliveryChannel.BARK -> null
            barkServerUrl.isBlank() -> stringsProvider.get(R.string.settings_error_server_empty)
            !isValidHttpUrl(barkServerUrl) -> stringsProvider.get(R.string.settings_error_server_invalid)
            else -> null
        }
        val barkDeviceKeyError = when {
            draft.deliveryChannel != DeliveryChannel.BARK -> null
            draft.barkDeviceKey.trim().isBlank() && draft.barkDeviceKeys.splitToList().isEmpty() ->
                stringsProvider.get(R.string.settings_error_device_key_required)
            else -> null
        }
        val barkLevelError = when {
            draft.deliveryChannel != DeliveryChannel.BARK -> null
            draft.barkLevel.trim() in listOf("critical", "active", "timeSensitive", "passive") -> null
            else -> stringsProvider.get(R.string.settings_error_level_invalid)
        }
        val barkVolumeError = if (draft.deliveryChannel != DeliveryChannel.BARK) {
            null
        } else {
            when (val value = draft.barkVolume.trim()) {
                "" -> null
                else -> value.toIntOrNull()?.takeIf { it in 0..10 }?.let { null }
                    ?: stringsProvider.get(R.string.settings_error_volume_invalid)
            }
        }
        val barkBadgeError = if (draft.deliveryChannel != DeliveryChannel.BARK) {
            null
        } else {
            when (val value = draft.barkBadge.trim()) {
                "" -> null
                else -> value.toIntOrNull()?.let { null } ?: stringsProvider.get(R.string.settings_error_badge_invalid)
            }
        }
        val barkActionError = when {
            draft.deliveryChannel != DeliveryChannel.BARK -> null
            draft.barkAction.trim() in listOf("", "alert") -> null
            else -> stringsProvider.get(R.string.settings_error_action_invalid)
        }
        val barkUrlError = if (draft.deliveryChannel == DeliveryChannel.BARK) validateOptionalHttpUrlOrScheme(draft.barkUrl) else null
        val barkIconError = if (draft.deliveryChannel == DeliveryChannel.BARK) validateOptionalHttpUrl(draft.barkIcon, "Icon URL") else null
        val barkImageError = if (draft.deliveryChannel == DeliveryChannel.BARK) validateOptionalHttpUrl(draft.barkImage, "Image URL") else null
        val telegramBotTokenError = when {
            draft.deliveryChannel != DeliveryChannel.TELEGRAM -> null
            draft.telegramBotToken.trim().isBlank() -> stringsProvider.get(R.string.settings_error_telegram_token_required)
            else -> null
        }
        val telegramChatIdError = when {
            draft.deliveryChannel != DeliveryChannel.TELEGRAM -> null
            draft.telegramChatId.trim().isBlank() -> stringsProvider.get(R.string.settings_error_telegram_chat_id_required)
            else -> null
        }
        val slackWebhookUrlError = when {
            draft.deliveryChannel != DeliveryChannel.SLACK -> null
            draft.slackWebhookUrl.trim().isBlank() -> stringsProvider.get(R.string.settings_error_slack_webhook_required)
            !isValidHttpUrl(draft.slackWebhookUrl.trim()) -> stringsProvider.get(R.string.settings_error_slack_webhook_invalid)
            else -> null
        }
        val emailSmtpHostError = when {
            draft.deliveryChannel != DeliveryChannel.EMAIL -> null
            draft.emailSmtpHost.trim().isBlank() -> stringsProvider.get(R.string.settings_error_email_host_required)
            else -> null
        }
        val emailSmtpPortError = when {
            draft.deliveryChannel != DeliveryChannel.EMAIL -> null
            draft.emailSmtpPort.trim().toIntOrNull() == null -> stringsProvider.get(R.string.settings_error_email_port_invalid)
            else -> null
        }
        val emailUsernameError = when {
            draft.deliveryChannel != DeliveryChannel.EMAIL -> null
            draft.emailUsername.trim().isBlank() -> stringsProvider.get(R.string.settings_error_email_username_required)
            else -> null
        }
        val emailPasswordError = when {
            draft.deliveryChannel != DeliveryChannel.EMAIL -> null
            draft.emailPassword.isBlank() -> stringsProvider.get(R.string.settings_error_email_password_required)
            else -> null
        }
        val emailFromAddressError = when {
            draft.deliveryChannel != DeliveryChannel.EMAIL -> null
            !isValidEmail(draft.emailFromAddress.trim()) -> stringsProvider.get(R.string.settings_error_email_from_invalid)
            else -> null
        }
        val emailToAddressError = when {
            draft.deliveryChannel != DeliveryChannel.EMAIL -> null
            !isValidEmail(draft.emailToAddress.trim()) -> stringsProvider.get(R.string.settings_error_email_to_invalid)
            else -> null
        }
        val dedupeSecondsError = when (draft.dedupeSeconds.toIntOrNull()) {
            null -> stringsProvider.get(R.string.settings_error_dedupe_invalid)
            in 0..600 -> null
            else -> stringsProvider.get(R.string.settings_error_dedupe_range)
        }
        return SettingsValidationState(
            barkServerUrlError = barkServerUrlError,
            barkDeviceKeyError = barkDeviceKeyError,
            barkLevelError = barkLevelError,
            barkVolumeError = barkVolumeError,
            barkBadgeError = barkBadgeError,
            barkActionError = barkActionError,
            barkUrlError = barkUrlError,
            barkIconError = barkIconError,
            barkImageError = barkImageError,
            telegramBotTokenError = telegramBotTokenError,
            telegramChatIdError = telegramChatIdError,
            slackWebhookUrlError = slackWebhookUrlError,
            emailSmtpHostError = emailSmtpHostError,
            emailSmtpPortError = emailSmtpPortError,
            emailUsernameError = emailUsernameError,
            emailPasswordError = emailPasswordError,
            emailFromAddressError = emailFromAddressError,
            emailToAddressError = emailToAddressError,
            dedupeSecondsError = dedupeSecondsError,
        )
    }

    private fun isValidHttpUrl(value: String): Boolean {
        return runCatching {
            val uri = URI(value)
            (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    private fun validateOptionalHttpUrl(value: String, fieldName: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return if (isValidHttpUrl(trimmed)) null else stringsProvider.get(R.string.settings_error_valid_url, fieldName)
    }

    private fun validateOptionalHttpUrlOrScheme(value: String): String? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return null
        return runCatching {
            val uri = URI(trimmed)
            if (!uri.scheme.isNullOrBlank()) null else stringsProvider.get(R.string.settings_error_jump_scheme)
        }.getOrElse { stringsProvider.get(R.string.settings_error_jump_invalid) }
    }

    private fun isValidEmail(value: String): Boolean {
        return value.contains('@') && value.substringAfter('@').contains('.')
    }
}

private fun SettingsDraft.toAppSettings(): AppSettings = AppSettings(
    forwardingEnabled = forwardingEnabled,
    cancelNotificationOnSuccess = cancelNotificationOnSuccess,
    preventChannelLoop = preventChannelLoop,
    deliveryChannel = deliveryChannel,
    barkServerUrl = barkServerUrl.trim(),
    barkDeviceKey = barkDeviceKey.trim(),
    barkDeviceKeys = barkDeviceKeys.splitToList(),
    barkLevel = barkLevel.trim(),
    barkVolume = barkVolume.trim().toIntOrNull(),
    barkBadge = barkBadge.trim().toIntOrNull(),
    barkCall = barkCall,
    barkAutoCopy = barkAutoCopy,
    barkCopy = barkCopy.trim(),
    barkSound = barkSound.trim(),
    barkIcon = barkIcon.trim(),
    barkImage = barkImage.trim(),
    barkGroupMode = barkGroupMode,
    barkGroupCustom = barkGroupCustom.trim(),
    barkCiphertext = barkCiphertext.trim(),
    barkIsArchive = barkIsArchive,
    barkUrl = barkUrl.trim(),
    barkAction = barkAction.trim(),
    barkNotificationId = barkNotificationId.trim(),
    barkDelete = barkDelete,
    barkUseMarkdown = barkUseMarkdown,
    telegramBotToken = telegramBotToken.trim(),
    telegramChatId = telegramChatId.trim(),
    telegramMessageThreadId = telegramMessageThreadId.trim(),
    telegramDisableNotification = telegramDisableNotification,
    telegramUseMarkdown = telegramUseMarkdown,
    slackWebhookUrl = slackWebhookUrl.trim(),
    slackUsername = slackUsername.trim(),
    slackIconEmoji = slackIconEmoji.trim(),
    emailSmtpHost = emailSmtpHost.trim(),
    emailSmtpPort = emailSmtpPort.trim().toIntOrNull() ?: 587,
    emailSecurityMode = emailSecurityMode,
    emailUsername = emailUsername.trim(),
    emailPassword = emailPassword,
    emailFromAddress = emailFromAddress.trim(),
    emailToAddress = emailToAddress.trim(),
    emailSubjectPrefix = emailSubjectPrefix.trim(),
    filterRuleSet = FilterRuleSet(
        enabled = filtersEnabled,
        allowedPackages = allowedPackages.splitToSet(),
        blockedPackages = blockedPackages.splitToSet(),
        keywordWhitelist = keywordWhitelist.splitToSet(),
        keywordBlacklist = keywordBlacklist.splitToSet(),
        excludeSystemNotifications = excludeSystem,
        excludeOngoingNotifications = excludeOngoing,
        excludeEmptyTextNotifications = excludeEmpty,
        dedupeWindowSeconds = dedupeSeconds.toIntOrNull() ?: 10,
        autoRetryEnabled = autoRetry,
    ),
)

private fun String.splitToSet(): Set<String> = split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .toSet()

private fun String.splitToList(): List<String> = split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
