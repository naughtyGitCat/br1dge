package com.example.notifybridge.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifybridge.domain.model.DeliveryAttempt
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.NotificationEvent
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.system.util.DeliveryWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogDetailUiState(
    val record: DeliveryRecord? = null,
    val notificationEvent: NotificationEvent? = null,
    val attempts: List<DeliveryAttempt> = emptyList(),
)

@HiltViewModel
class LogDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deliveryLogRepository: DeliveryLogRepository,
    private val deliveryWorkScheduler: DeliveryWorkScheduler,
) : ViewModel() {
    private val eventId: String = savedStateHandle.get<String>("eventId").orEmpty()

    val uiState: StateFlow<LogDetailUiState> = combine(
        deliveryLogRepository.observeDetail(eventId),
        deliveryLogRepository.observeNotificationEvent(eventId),
        deliveryLogRepository.observeAttempts(eventId),
    ) { record: DeliveryRecord?, notificationEvent: NotificationEvent?, attempts: List<DeliveryAttempt> ->
        LogDetailUiState(
            record = record,
            notificationEvent = notificationEvent,
            attempts = attempts,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LogDetailUiState())

    fun retry() {
        viewModelScope.launch {
            deliveryLogRepository.markPending(eventId)
            deliveryWorkScheduler.enqueueNow()
        }
    }
}
