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
                Text("事件 ID：$eventId")
                val record = uiState.record
                if (record == null) {
                    Text("暂无详情")
                } else {
                    Text("App：${record.appName}")
                    Text("状态：${record.status}")
                    Text("标题：${record.title ?: "(无标题)"}")
                    Text("正文：${record.text ?: "(无正文)"}")
                    Text("错误：${record.errorMessage ?: "无"}")
                    Text("响应码：${record.responseCode?.toString() ?: "无"}")
                    Text("Payload JSON：")
                    Text(record.payloadJson.ifBlank { "(无 payload)" })
                }
            }
        }
        if (uiState.attempts.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("投递尝试历史")
                    uiState.attempts.forEach { attempt ->
                        Text(
                            buildString {
                                append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(attempt.createdAt)))
                                append(" | code=")
                                append(attempt.responseCode ?: "-")
                                append(" | error=")
                                append(attempt.errorMessage ?: "none")
                            }
                        )
                    }
                }
            }
        }
    }
}
