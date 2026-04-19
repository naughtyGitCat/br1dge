package com.example.notifybridge.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uk.deprecated.notifybridge.R
import com.example.notifybridge.core.common.StringsProvider
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.BarkGroupMode
import com.example.notifybridge.domain.model.FilterRuleSet
import com.example.notifybridge.domain.model.ForwardResult
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import com.example.notifybridge.domain.usecase.UpdateSettingsUseCase
import com.example.notifybridge.system.util.DebugExportManager
import com.example.notifybridge.system.util.InstalledAppInfo
import com.example.notifybridge.system.util.InstalledAppsProvider
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
    data object ClearLogs : SettingsAction()
    data object ExportDebug : SettingsAction()
}

data class SettingsDraft(
    val forwardingEnabled: Boolean,
    val cancelNotificationOnSuccess: Boolean,
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
    settingsRepository: SettingsRepository,
    private val stringsProvider: StringsProvider,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val forwardRepository: ForwardRepository,
    private val deliveryLogRepository: DeliveryLogRepository,
    private val debugExportManager: DebugExportManager,
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
            barkServerUrl.isBlank() -> stringsProvider.get(R.string.settings_error_server_empty)
            !isValidHttpUrl(barkServerUrl) -> stringsProvider.get(R.string.settings_error_server_invalid)
            else -> null
        }
        val barkDeviceKeyError = when {
            draft.barkDeviceKey.trim().isBlank() && draft.barkDeviceKeys.splitToList().isEmpty() -> stringsProvider.get(R.string.settings_error_device_key_required)
            else -> null
        }
        val barkLevelError = when (draft.barkLevel.trim()) {
            "critical", "active", "timeSensitive", "passive" -> null
            else -> stringsProvider.get(R.string.settings_error_level_invalid)
        }
        val barkVolumeError = when (val value = draft.barkVolume.trim()) {
            "" -> null
            else -> value.toIntOrNull()?.takeIf { it in 0..10 }?.let { null } ?: stringsProvider.get(R.string.settings_error_volume_invalid)
        }
        val barkBadgeError = when (val value = draft.barkBadge.trim()) {
            "" -> null
            else -> value.toIntOrNull()?.let { null } ?: stringsProvider.get(R.string.settings_error_badge_invalid)
        }
        val barkActionError = when (draft.barkAction.trim()) {
            "", "alert" -> null
            else -> stringsProvider.get(R.string.settings_error_action_invalid)
        }
        val barkUrlError = validateOptionalHttpUrlOrScheme(draft.barkUrl)
        val barkIconError = validateOptionalHttpUrl(draft.barkIcon, "Icon URL")
        val barkImageError = validateOptionalHttpUrl(draft.barkImage, "Image URL")
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
}

private fun SettingsDraft.toAppSettings(): AppSettings = AppSettings(
    forwardingEnabled = forwardingEnabled,
    cancelNotificationOnSuccess = cancelNotificationOnSuccess,
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
