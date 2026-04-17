package com.example.notifybridge.domain.repository

import com.example.notifybridge.domain.model.DashboardState
import com.example.notifybridge.domain.model.DeliveryAttempt
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.DeliveryStatus
import com.example.notifybridge.domain.model.NotificationEvent
import kotlinx.coroutines.flow.Flow

interface DeliveryLogRepository {
    suspend fun enqueueNotification(event: NotificationEvent): Long
    suspend fun getPendingEvents(limit: Int = 20): List<NotificationEvent>
    suspend fun markSending(eventId: String)
    suspend fun markDelivered(eventId: String)
    suspend fun markFailed(eventId: String, errorMessage: String, retrying: Boolean, responseCode: Int? = null)
    suspend fun markPending(eventId: String)
    suspend fun appendAttempt(eventId: String, errorMessage: String?, responseCode: Int?, payloadJson: String)
    fun observeLogs(status: DeliveryStatus?): Flow<List<DeliveryRecord>>
    fun observeDetail(eventId: String): Flow<DeliveryRecord?>
    fun observeNotificationEvent(eventId: String): Flow<NotificationEvent?>
    fun observeAttempts(eventId: String): Flow<List<DeliveryAttempt>>
    fun observeDashboardState(): Flow<DashboardState>
    suspend fun getLastAcceptedAtForDedupe(packageName: String, title: String?, text: String?): Long?
    suspend fun clearLogs()
    suspend fun exportDebugSnapshot(): String
}
