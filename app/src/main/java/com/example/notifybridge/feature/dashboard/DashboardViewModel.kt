package com.example.notifybridge.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifybridge.domain.model.DashboardState
import com.example.notifybridge.domain.usecase.GetDashboardStateUseCase
import com.example.notifybridge.system.util.NotificationAccessManager
import com.example.notifybridge.system.util.NotificationTestManager
import com.example.notifybridge.system.util.SystemSettingsNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardAction {
    data object OpenNotificationAccess : DashboardAction()
    data object OpenBatteryOptimization : DashboardAction()
    data object SendTestNotification : DashboardAction()
}

data class DashboardUiState(
    val dashboardState: DashboardState = DashboardState(),
    val listenerEnabled: Boolean = false,
    val lastActionMessage: String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardStateUseCase: GetDashboardStateUseCase,
    private val notificationAccessManager: NotificationAccessManager,
    private val systemSettingsNavigator: SystemSettingsNavigator,
    private val notificationTestManager: NotificationTestManager,
) : ViewModel() {

    private val actionMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardStateUseCase(),
        actionMessage,
    ) { dashboardState, actionMsg ->
        DashboardUiState(
            dashboardState = dashboardState,
            listenerEnabled = notificationAccessManager.isNotificationAccessEnabled(),
            lastActionMessage = actionMsg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun onAction(action: DashboardAction) {
        viewModelScope.launch {
            val message = when (action) {
                DashboardAction.OpenNotificationAccess -> if (systemSettingsNavigator.openNotificationAccessSettings()) {
                    "已打开通知访问设置"
                } else {
                    "无法打开通知访问设置"
                }
                DashboardAction.OpenBatteryOptimization -> if (systemSettingsNavigator.openBatteryOptimizationSettings()) {
                    "已尝试打开电池优化设置"
                } else {
                    "无法打开电池优化设置"
                }
                DashboardAction.SendTestNotification -> if (notificationTestManager.sendLocalTestNotification()) {
                    "已发送本地测试通知"
                } else {
                    "测试通知发送失败，请确认通知权限"
                }
            }
            actionMessage.value = message
        }
    }
}
