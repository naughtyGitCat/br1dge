package com.example.notifybridge.feature.logs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
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
fun LogsScreenRoute(
    contentPadding: PaddingValues,
    onOpenDetail: (String) -> Unit,
    viewModel: LogsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LogsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onFilterSelected = viewModel::setFilter,
        onSearchQueryChanged = viewModel::updateSearchQuery,
        onOpenDetail = onOpenDetail,
        onRetry = viewModel::retry,
        onRetryVisible = { viewModel.retryVisible(uiState.records) },
        onLoadMore = viewModel::loadMore,
    )
}

@Composable
private fun LogsScreen(
    contentPadding: PaddingValues,
    uiState: LogsUiState,
    onFilterSelected: (LogStatusFilter) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onRetry: (String) -> Unit,
    onRetryVisible: () -> Unit,
    onLoadMore: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LogStatusFilter.entries.forEach { filter ->
                    FilterChip(
                        onClick = { onFilterSelected(filter) },
                        selected = uiState.selectedFilter == filter,
                        label = { Text(filter.name) }
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索 app / 标题 / 正文 / 错误") },
                singleLine = true,
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("匹配记录：${uiState.totalMatchedCount}")
                    if (uiState.canBulkRetry) {
                        Button(onClick = onRetryVisible) {
                            Text("批量重试当前结果")
                        }
                    }
                    uiState.message?.let { Text(it) }
                }
            }
        }
        if (uiState.records.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("暂无匹配日志")
                        Text("可以调整状态筛选或搜索关键字，或等待新的通知进入队列。")
                    }
                }
            }
        }
        items(uiState.records, key = { it.eventId }) { record ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail(record.eventId) }
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(record.appName)
                    Text(record.title ?: "(无标题)")
                    Text(record.text ?: "(无正文)")
                    Text("状态：${record.status.name} | 重试：${record.attemptCount}")
                    record.errorMessage?.let { Text("错误：$it") }
                    Text(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.updatedAt)))
                    if (record.status == com.example.notifybridge.domain.model.DeliveryStatus.FAILED ||
                        record.status == com.example.notifybridge.domain.model.DeliveryStatus.RETRYING
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onRetry(record.eventId) }) {
                                Text("重新入队")
                            }
                            AssistChip(
                                onClick = { onOpenDetail(record.eventId) },
                                label = { Text("查看详情") }
                            )
                        }
                    }
                }
            }
        }
        if (uiState.canLoadMore) {
            item {
                Button(
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("加载更多")
                }
            }
        }
    }
}
