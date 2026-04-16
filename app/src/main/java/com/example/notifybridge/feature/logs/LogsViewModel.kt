package com.example.notifybridge.feature.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.DeliveryStatus
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
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
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val deliveryLogRepository: DeliveryLogRepository,
) : ViewModel() {

    private val filterState = MutableStateFlow(LogStatusFilter.ALL)

    val uiState: StateFlow<LogsUiState> = combine(
        filterState,
        filterState.flatMapLatest { filter -> deliveryLogRepository.observeLogs(filter.status) },
    ) { filter, records ->
        LogsUiState(selectedFilter = filter, records = records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogsUiState())

    fun setFilter(filter: LogStatusFilter) {
        filterState.value = filter
    }
}
