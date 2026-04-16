package com.example.notifybridge.feature.logs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
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
        onOpenDetail = onOpenDetail,
    )
}

@Composable
private fun LogsScreen(
    contentPadding: PaddingValues,
    uiState: LogsUiState,
    onFilterSelected: (LogStatusFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LogStatusFilter.entries.forEach { filter ->
                    AssistChip(
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter.name) }
                    )
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
                }
            }
        }
    }
}
