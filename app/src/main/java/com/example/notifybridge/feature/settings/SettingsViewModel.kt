package com.example.notifybridge.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.FilterRuleSet
import com.example.notifybridge.domain.model.ForwardResult
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import com.example.notifybridge.domain.usecase.UpdateSettingsUseCase
import com.example.notifybridge.system.util.DebugExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val webhookUrl: String,
    val bearerToken: String,
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
    val webhookUrlError: String? = null,
    val dedupeSecondsError: String? = null,
) {
    val isValid: Boolean
        get() = webhookUrlError == null && dedupeSecondsError == null
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
    val message: String? = null,
    val exportingPath: String? = null,
    val validation: SettingsValidationState = SettingsValidationState(),
    val testSendState: TestSendState = TestSendState.Idle,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val forwardRepository: ForwardRepository,
    private val deliveryLogRepository: DeliveryLogRepository,
    private val debugExportManager: DebugExportManager,
) : ViewModel() {

    private val ephemeralState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeSettings(),
        ephemeralState,
    ) { settings, ephemeral ->
        SettingsUiState(
            settings = settings,
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

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.Save -> {
                    val validation = validate(action.draft)
                    if (!validation.isValid) {
                        ephemeralState.value = ephemeralState.value.copy(
                            message = "请先修正设置项",
                            validation = validation,
                        )
                        return@launch
                    }
                    updateSettingsUseCase(action.draft.toAppSettings())
                    ephemeralState.value = ephemeralState.value.copy(
                        message = "设置已保存",
                        validation = validation,
                    )
                }
                is SettingsAction.TestSend -> {
                    val validation = validate(action.draft)
                    if (!validation.isValid) {
                        ephemeralState.value = ephemeralState.value.copy(
                            message = "请先修正设置项",
                            validation = validation,
                            testSendState = TestSendState.Failure("表单校验未通过"),
                        )
                        return@launch
                    }
                    ephemeralState.value = ephemeralState.value.copy(
                        message = "正在测试连通性...",
                        validation = validation,
                        testSendState = TestSendState.Running,
                    )
                    val message = when (val result = forwardRepository.sendTestPayload(action.draft.toAppSettings())) {
                        is ForwardResult.Success -> {
                            ephemeralState.value = ephemeralState.value.copy(
                                testSendState = TestSendState.Success("Webhook 连通性正常"),
                            )
                            "测试发送成功"
                        }
                        is ForwardResult.Failure -> {
                            ephemeralState.value = ephemeralState.value.copy(
                                testSendState = TestSendState.Failure(result.error.message),
                            )
                            "测试发送失败：${result.error.message}"
                        }
                    }
                    ephemeralState.value = ephemeralState.value.copy(message = message)
                }
                SettingsAction.ClearLogs -> {
                    deliveryLogRepository.clearLogs()
                    ephemeralState.value = ephemeralState.value.copy(message = "日志已清空")
                }
                SettingsAction.ExportDebug -> {
                    val file = debugExportManager.exportJson(deliveryLogRepository.exportDebugSnapshot())
                    ephemeralState.value = ephemeralState.value.copy(
                        message = "调试信息已导出",
                        exportingPath = file.absolutePath,
                    )
                }
            }
        }
    }

    private fun validate(draft: SettingsDraft): SettingsValidationState {
        val webhookUrl = draft.webhookUrl.trim()
        val webhookUrlError = when {
            webhookUrl.isBlank() -> "Webhook URL 不能为空"
            !isValidHttpUrl(webhookUrl) -> "请输入有效的 http/https URL"
            else -> null
        }
        val dedupeSecondsError = when (draft.dedupeSeconds.toIntOrNull()) {
            null -> "去重秒数必须是整数"
            in 0..600 -> null
            else -> "去重秒数请填写 0 到 600"
        }
        return SettingsValidationState(
            webhookUrlError = webhookUrlError,
            dedupeSecondsError = dedupeSecondsError,
        )
    }

    private fun isValidHttpUrl(value: String): Boolean {
        return runCatching {
            val uri = URI(value)
            (uri.scheme == "http" || uri.scheme == "https") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }
}

private fun SettingsDraft.toAppSettings(): AppSettings = AppSettings(
    forwardingEnabled = forwardingEnabled,
    webhookUrl = webhookUrl.trim(),
    bearerToken = bearerToken.trim(),
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
