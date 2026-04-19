package com.example.notifybridge.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uk.deprecated.notifybridge.R
import com.example.notifybridge.core.common.StringsProvider
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
    private val stringsProvider: StringsProvider,
    private val notificationAccessManager: NotificationAccessManager,
    private val systemSettingsNavigator: SystemSettingsNavigator,
    private val notificationTestManager: NotificationTestManager,
) : ViewModel() {

    private val actionMessage = MutableStateFlow<String?>(null)
    private val listenerEnabled = MutableStateFlow(notificationAccessManager.isNotificationAccessEnabled())

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardStateUseCase(),
        actionMessage,
        listenerEnabled,
    ) { dashboardState, actionMsg, accessEnabled ->
        DashboardUiState(
            dashboardState = dashboardState,
            listenerEnabled = accessEnabled,
            lastActionMessage = actionMsg,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun refreshStatus() {
        listenerEnabled.value = notificationAccessManager.isNotificationAccessEnabled()
    }

    fun onAction(action: DashboardAction) {
        viewModelScope.launch {
            val message = when (action) {
                DashboardAction.OpenNotificationAccess -> if (systemSettingsNavigator.openNotificationAccessSettings()) {
                    stringsProvider.get(R.string.dashboard_msg_notification_access_opened)
                } else {
                    stringsProvider.get(R.string.dashboard_msg_notification_access_failed)
                }
                DashboardAction.OpenBatteryOptimization -> if (systemSettingsNavigator.openBatteryOptimizationSettings()) {
                    stringsProvider.get(R.string.dashboard_msg_battery_opened)
                } else {
                    stringsProvider.get(R.string.dashboard_msg_battery_failed)
                }
                DashboardAction.SendTestNotification -> if (notificationTestManager.sendLocalTestNotification()) {
                    stringsProvider.get(R.string.dashboard_msg_test_sent)
                } else {
                    stringsProvider.get(R.string.dashboard_msg_test_failed)
                }
            }
            actionMessage.value = message
            listenerEnabled.value = notificationAccessManager.isNotificationAccessEnabled()
        }
    }
}
