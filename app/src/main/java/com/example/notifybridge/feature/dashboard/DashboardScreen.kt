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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                    Text("NotifyBridge 状态总览", style = MaterialTheme.typography.titleLarge)
                    Text("通知监听权限：${if (uiState.listenerEnabled) "已开启" else "未开启"}")
                    Text("待发送队列：${uiState.dashboardState.pendingCount}")
                    Text("今日成功：${uiState.dashboardState.todaySuccessCount}")
                    Text("今日失败：${uiState.dashboardState.todayFailureCount}")
                    Text("最近一次成功：${uiState.dashboardState.lastSuccessAt?.toFriendlyTime() ?: "暂无"}")
                    Text("最近一次失败：${uiState.dashboardState.lastFailureReason ?: "暂无"}")
                    uiState.lastActionMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(DashboardAction.OpenNotificationAccess) }, modifier = Modifier.weight(1f)) {
                    Text("通知访问设置")
                }
                Button(onClick = { onAction(DashboardAction.OpenBatteryOptimization) }, modifier = Modifier.weight(1f)) {
                    Text("电池优化设置")
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAction(DashboardAction.SendTestNotification) }, modifier = Modifier.weight(1f)) {
                    Text("生成测试通知")
                }
                Button(onClick = onNavigateToSettings, modifier = Modifier.weight(1f)) {
                    Text("前往设置")
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
