package com.example.notifybridge.feature.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.DeliveryStatus
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.system.util.DeliveryWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LogStatusFilter(val status: DeliveryStatus?) {
    ALL(null),
    SUCCESS(DeliveryStatus.SUCCESS),
    FAILED(DeliveryStatus.FAILED),
    RETRYING(DeliveryStatus.RETRYING),
    PENDING(DeliveryStatus.PENDING),
}

data class LogsUiState(
    val selectedFilter: LogStatusFilter = LogStatusFilter.ALL,
    val records: List<DeliveryRecord> = emptyList(),
    val searchQuery: String = "",
    val totalMatchedCount: Int = 0,
    val canLoadMore: Boolean = false,
    val canBulkRetry: Boolean = false,
    val message: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LogsViewModel @Inject constructor(
    private val deliveryLogRepository: DeliveryLogRepository,
    private val deliveryWorkScheduler: DeliveryWorkScheduler,
) : ViewModel() {

    private val filterState = MutableStateFlow(LogStatusFilter.ALL)
    private val queryState = MutableStateFlow("")
    private val visibleCountState = MutableStateFlow(DEFAULT_PAGE_SIZE)
    private val messageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LogsUiState> = combine(
        filterState,
        queryState,
        visibleCountState,
        messageState,
        combine(
            combine(filterState, queryState, visibleCountState) { filter, query, limit ->
                Triple(filter, query, limit)
            }.flatMapLatest { (filter, query, limit) ->
                deliveryLogRepository.observeLogs(
                    status = filter.status,
                    query = query,
                    limit = limit,
                )
            },
            combine(filterState, queryState) { filter, query ->
                filter to query
            }.flatMapLatest { (filter, query) ->
                deliveryLogRepository.observeLogCount(
                    status = filter.status,
                    query = query,
                )
            }
        ) { records, totalCount -> records to totalCount }
    ) { filter, query, _, message, payload ->
        val (records, totalCount) = payload
        val retryableCount = records.count {
            it.status == DeliveryStatus.FAILED || it.status == DeliveryStatus.RETRYING
        }
        LogsUiState(
            selectedFilter = filter,
            records = records,
            searchQuery = query,
            totalMatchedCount = totalCount,
            canLoadMore = totalCount > records.size,
            canBulkRetry = retryableCount > 1,
            message = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogsUiState())

    fun setFilter(filter: LogStatusFilter) {
        filterState.value = filter
        visibleCountState.value = DEFAULT_PAGE_SIZE
    }

    fun updateSearchQuery(query: String) {
        queryState.value = query
        visibleCountState.value = DEFAULT_PAGE_SIZE
    }

    fun loadMore() {
        visibleCountState.update { it + DEFAULT_PAGE_SIZE }
    }

    fun retry(eventId: String) {
        viewModelScope.launch {
            deliveryLogRepository.markPending(eventId)
            deliveryWorkScheduler.enqueueNow()
            messageState.value = "已重新入队 1 条记录"
        }
    }

    fun retryVisible(records: List<DeliveryRecord>) {
        val retryableIds = records
            .filter { it.status == DeliveryStatus.FAILED || it.status == DeliveryStatus.RETRYING }
            .map { it.eventId }
            .distinct()
        if (retryableIds.isEmpty()) {
            messageState.value = "当前没有可重试记录"
            return
        }
        viewModelScope.launch {
            retryableIds.forEach { deliveryLogRepository.markPending(it) }
            deliveryWorkScheduler.enqueueNow()
            messageState.value = "已重新入队 ${retryableIds.size} 条记录"
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 50
    }
}
