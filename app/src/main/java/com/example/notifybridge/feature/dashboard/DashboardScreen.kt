package com.example.notifybridge.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.ngcat.notifybridge.R
import com.example.notifybridge.core.common.NotifyBridgeStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreenRoute(
    contentPadding: PaddingValues,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    DashboardScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToSettings = onNavigateToSettings,
    )
}

@Composable
private fun DashboardScreen(
    contentPadding: PaddingValues,
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.dashboard_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(if (uiState.listenerEnabled) R.string.dashboard_listener_enabled else R.string.dashboard_listener_disabled))
                    Text(stringResource(R.string.dashboard_pending_count, uiState.dashboardState.pendingCount))
                    Text(stringResource(R.string.dashboard_retrying_count, uiState.dashboardState.retryingCount))
                    Text(stringResource(R.string.dashboard_today_success, uiState.dashboardState.todaySuccessCount))
                    Text(stringResource(R.string.dashboard_today_failure, uiState.dashboardState.todayFailureCount))
                    Text(stringResource(R.string.dashboard_last_success, uiState.dashboardState.lastSuccessAt?.toFriendlyTime() ?: stringResource(R.string.common_none)))
                    Text(stringResource(R.string.dashboard_last_failure, uiState.dashboardState.lastFailureReason ?: stringResource(R.string.common_none)))
                    Text(
                        stringResource(R.string.dashboard_next_retry,
                            uiState.dashboardState.nextRetryAt?.let {
                                "${it.toFriendlyTime()} (${it.toRelativeRetryText()})"
                            } ?: stringResource(R.string.common_none)
                        )
                    )
                    uiState.lastActionMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(DashboardAction.OpenNotificationAccess) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.dashboard_open_notification_access))
                }
                Button(onClick = { onAction(DashboardAction.OpenBatteryOptimization) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.dashboard_open_battery_optimization))
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(DashboardAction.SendTestNotification) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.dashboard_send_test_notification))
                }
                Button(onClick = onNavigateToSettings, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.dashboard_go_settings))
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun Long.toFriendlyTime(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(this))
}

private fun Long.toRelativeRetryText(now: Long = System.currentTimeMillis()): String {
    val delta = this - now
    if (delta <= 0) return NotifyBridgeStrings.commonDueSoon()
    val totalSeconds = delta / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> NotifyBridgeStrings.afterMinSec(minutes, seconds)
        minutes > 0 -> NotifyBridgeStrings.afterMinutes(minutes)
        else -> NotifyBridgeStrings.afterSeconds(seconds)
    }
}
