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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uk.deprecated.notifybridge.R
import com.example.notifybridge.core.common.NotifyBridgeStrings
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
            Text(stringResource(R.string.detail_back))
        }
        Button(onClick = onRetry) {
            Text(stringResource(R.string.detail_retry))
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.detail_current_status), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.detail_event_id, eventId))
                val record = uiState.record
                if (record == null) {
                    Text(stringResource(R.string.detail_empty))
                } else {
                    Text(stringResource(R.string.detail_app, record.appName))
                    Text(stringResource(R.string.detail_status, record.status.toString()))
                    record.nextRetryAt?.let {
                        Text(stringResource(R.string.detail_next_retry, it.toFriendlyTime(), it.toRelativeRetryText()))
                    }
                    Text(stringResource(R.string.detail_attempt_count, record.attemptCount))
                    Text(stringResource(R.string.detail_title, record.title ?: stringResource(R.string.logs_no_title)))
                    Text(stringResource(R.string.detail_body, record.text ?: stringResource(R.string.logs_no_body)))
                    Text(stringResource(R.string.detail_error, record.errorMessage ?: stringResource(R.string.detail_empty_value)))
                    Text(stringResource(R.string.detail_response_code, record.responseCode?.toString() ?: stringResource(R.string.detail_empty_value)))
                    Text(stringResource(R.string.detail_payload_json))
                    Text(record.payloadJson.ifBlank { stringResource(R.string.detail_no_payload) })
                }
            }
        }
        uiState.record?.toFailurePresentation()?.let { failure ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.detail_failure_diag), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.detail_failure_category, failure.category))
                    Text(stringResource(R.string.detail_failure_summary, failure.summary))
                    Text(stringResource(R.string.detail_failure_suggestion, failure.suggestion))
                    Text(stringResource(R.string.detail_failure_retryable, stringResource(if (failure.retryable) R.string.detail_yes else R.string.detail_no)))
                }
            }
        }
        uiState.notificationEvent?.let { event ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.detail_raw_notification), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.detail_package_name, event.packageName))
                    Text(stringResource(R.string.detail_notification_key, event.notificationKey))
                    Text(stringResource(R.string.detail_received_at, event.receivedAt.toFriendlyTime()))
                    Text(stringResource(R.string.detail_post_time, event.postTime.toFriendlyTime()))
                    Text(stringResource(R.string.detail_subtext, event.subText ?: stringResource(R.string.detail_empty_value)))
                    Text(stringResource(R.string.detail_ongoing, stringResource(if (event.ongoing) R.string.detail_yes else R.string.detail_no)))
                    Text(stringResource(R.string.detail_clearable, stringResource(if (event.clearable) R.string.detail_yes else R.string.detail_no)))
                    Text(stringResource(R.string.detail_system_notification, stringResource(if (event.isSystemNotification) R.string.detail_yes else R.string.detail_no)))
                }
            }
        }
        if (uiState.attempts.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.detail_attempt_history), style = MaterialTheme.typography.titleMedium)
                    uiState.attempts.forEach { attempt ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(stringResource(R.string.detail_attempt_time, attempt.createdAt.toFriendlyTime()))
                                Text(stringResource(R.string.detail_attempt_response, attempt.responseCode?.toString() ?: stringResource(R.string.detail_empty_value)))
                                Text(stringResource(R.string.detail_attempt_error, attempt.errorMessage ?: stringResource(R.string.detail_empty_value)))
                                attempt.toFailurePresentation()?.let { failure ->
                                    Text(stringResource(R.string.detail_failure_class, failure.category))
                                    Text(stringResource(R.string.detail_failure_advice, failure.suggestion))
                                }
                                Text(stringResource(R.string.detail_request_payload))
                                Text(attempt.payloadJson.ifBlank { stringResource(R.string.detail_empty_payload) })
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
