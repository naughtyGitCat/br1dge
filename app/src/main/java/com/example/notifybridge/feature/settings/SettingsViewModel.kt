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
import javax.inject.Inject

sealed class SettingsAction {
    data class Save(val settings: AppSettings) : SettingsAction()
    data object TestSend : SettingsAction()
    data object ClearLogs : SettingsAction()
    data object ExportDebug : SettingsAction()
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(
        filterRuleSet = FilterRuleSet(enabled = true)
    ),
    val message: String? = null,
    val exportingPath: String? = null,
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
                    updateSettingsUseCase(action.settings)
                    ephemeralState.value = ephemeralState.value.copy(message = "设置已保存")
                }
                SettingsAction.TestSend -> {
                    val message = when (val result = forwardRepository.sendTestPayload()) {
                        is ForwardResult.Success -> "测试发送成功"
                        is ForwardResult.Failure -> "测试发送失败：${result.error.message}"
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
}
