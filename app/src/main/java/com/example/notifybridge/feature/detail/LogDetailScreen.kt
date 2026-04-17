package com.example.notifybridge.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun LogDetailScreenRoute(
    contentPadding: PaddingValues,
    eventId: String,
    onBack: () -> Unit,
    viewModel: LogDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LogDetailScreen(
        contentPadding = contentPadding,
        eventId = eventId,
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
    )
}

@Composable
private fun LogDetailScreen(
    contentPadding: PaddingValues,
    eventId: String,
    uiState: LogDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) {
            Text("返回")
        }
        Button(onClick = onRetry) {
            Text("重新入队并重试")
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("当前投递状态", style = MaterialTheme.typography.titleMedium)
                Text("事件 ID：$eventId")
                val record = uiState.record
                if (record == null) {
                    Text("暂无详情")
                } else {
                    Text("App：${record.appName}")
                    Text("状态：${record.status}")
                    record.nextRetryAt?.let {
                        Text("预计下次重试：${it.toFriendlyTime()}（${it.toRelativeRetryText()}）")
                    }
                    Text("已尝试次数：${record.attemptCount}")
                    Text("标题：${record.title ?: "(无标题)"}")
                    Text("正文：${record.text ?: "(无正文)"}")
                    Text("错误：${record.errorMessage ?: "无"}")
                    Text("响应码：${record.responseCode?.toString() ?: "无"}")
                    Text("Payload JSON：")
                    Text(record.payloadJson.ifBlank { "(无 payload)" })
                }
            }
        }
        uiState.record?.toFailurePresentation()?.let { failure ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("失败诊断", style = MaterialTheme.typography.titleMedium)
                    Text("分类：${failure.category}")
                    Text("摘要：${failure.summary}")
                    Text("建议：${failure.suggestion}")
                    Text("是否适合重试：${if (failure.retryable) "是" else "否"}")
                }
            }
        }
        uiState.notificationEvent?.let { event ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("原始通知元数据", style = MaterialTheme.typography.titleMedium)
                    Text("来源包名：${event.packageName}")
                    Text("通知键：${event.notificationKey}")
                    Text("接收时间：${event.receivedAt.toFriendlyTime()}")
                    Text("发送时间：${event.postTime.toFriendlyTime()}")
                    Text("SubText：${event.subText ?: "(无)"}")
                    Text("ongoing：${if (event.ongoing) "是" else "否"}")
                    Text("clearable：${if (event.clearable) "是" else "否"}")
                    Text("系统通知：${if (event.isSystemNotification) "是" else "否"}")
                }
            }
        }
        if (uiState.attempts.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("投递尝试历史", style = MaterialTheme.typography.titleMedium)
                    uiState.attempts.forEach { attempt ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("尝试时间：${attempt.createdAt.toFriendlyTime()}")
                                Text("响应码：${attempt.responseCode?.toString() ?: "无"}")
                                Text("错误信息：${attempt.errorMessage ?: "无"}")
                                attempt.toFailurePresentation()?.let { failure ->
                                    Text("失败分类：${failure.category}")
                                    Text("处理建议：${failure.suggestion}")
                                }
                                Text("请求 Payload：")
                                Text(attempt.payloadJson.ifBlank { "(空 payload)" })
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Long.toFriendlyTime(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(this))
}

private fun Long.toRelativeRetryText(now: Long = System.currentTimeMillis()): String {
    val delta = this - now
    if (delta <= 0) return "即将执行"
    val totalSeconds = delta / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> "${minutes}分${seconds}秒后"
        minutes > 0 -> "${minutes}分钟后"
        else -> "${seconds}秒后"
    }
}
