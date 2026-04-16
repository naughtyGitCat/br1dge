package com.example.notifybridge.data.repository

import com.example.notifybridge.data.mapper.toDomain
import com.example.notifybridge.data.mapper.toDomain
import com.example.notifybridge.data.mapper.toEntity
import com.example.notifybridge.core.database.dao.DeliveryAttemptDao
import com.example.notifybridge.core.database.dao.NotificationEventDao
import com.example.notifybridge.core.database.dao.OutboxDao
import com.example.notifybridge.core.database.entity.DeliveryAttemptEntity
import com.example.notifybridge.core.database.entity.OutboxEntity
import com.example.notifybridge.domain.model.DashboardState
import com.example.notifybridge.domain.model.DeliveryAttempt
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.DeliveryStatus
import com.example.notifybridge.domain.model.NotificationEvent
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryLogRepositoryImpl @Inject constructor(
    private val notificationEventDao: NotificationEventDao,
    private val outboxDao: OutboxDao,
    private val deliveryAttemptDao: DeliveryAttemptDao,
) : DeliveryLogRepository {

    override suspend fun enqueueNotification(event: NotificationEvent): Long {
        val now = System.currentTimeMillis()
        notificationEventDao.insert(event.toEntity())
        return outboxDao.insert(
            OutboxEntity(
                eventId = event.eventId,
                status = DeliveryStatus.PENDING,
                attemptCount = 0,
                errorMessage = null,
                responseCode = null,
                payloadJson = "",
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun getPendingEvents(limit: Int): List<NotificationEvent> {
        return notificationEventDao.getPendingEvents(limit).map { it.toDomain() }
    }

    override suspend fun markSending(eventId: String) {
        outboxDao.updateStatus(eventId, DeliveryStatus.RETRYING, System.currentTimeMillis())
    }

    override suspend fun markDelivered(eventId: String) {
        outboxDao.updateStatus(eventId, DeliveryStatus.SUCCESS, System.currentTimeMillis())
    }

    override suspend fun markFailed(eventId: String, errorMessage: String, retrying: Boolean, responseCode: Int?) {
        outboxDao.markFailure(
            eventId = eventId,
            status = if (retrying) DeliveryStatus.RETRYING else DeliveryStatus.FAILED,
            errorMessage = errorMessage,
            responseCode = responseCode,
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun markPending(eventId: String) {
        outboxDao.updateStatus(eventId, DeliveryStatus.PENDING, System.currentTimeMillis())
    }

    override suspend fun appendAttempt(eventId: String, errorMessage: String?, responseCode: Int?, payloadJson: String) {
        outboxDao.updatePayloadSnapshot(eventId, payloadJson, System.currentTimeMillis())
        deliveryAttemptDao.insert(
            DeliveryAttemptEntity(
                eventId = eventId,
                payloadJson = payloadJson,
                responseCode = responseCode,
                errorMessage = errorMessage,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    override fun observeLogs(status: DeliveryStatus?): Flow<List<DeliveryRecord>> {
        val source = if (status == null) outboxDao.observeAll() else outboxDao.observeByStatus(status)
        return source.map { outboxes ->
            outboxes.mapNotNull { outbox ->
                val event = notificationEventDao.getById(outbox.eventId) ?: return@mapNotNull null
                outbox.toDomain(appName = event.appName, title = event.title, text = event.text)
            }
        }
    }

    override fun observeDetail(eventId: String): Flow<DeliveryRecord?> {
        return outboxDao.observeByEventId(eventId).map { outbox ->
            val entity = outbox ?: return@map null
            val event = notificationEventDao.getById(entity.eventId) ?: return@map null
            entity.toDomain(appName = event.appName, title = event.title, text = event.text)
        }
    }

    override fun observeAttempts(eventId: String): Flow<List<DeliveryAttempt>> {
        return deliveryAttemptDao.observeByEventId(eventId).map { attempts ->
            attempts.map { it.toDomain() }
        }
    }

    override fun observeDashboardState(): Flow<DashboardState> {
        val todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return combine(
            outboxDao.observePendingCount(),
            outboxDao.observeLastSuccessAt(),
            outboxDao.observeLastFailureReason(),
            outboxDao.observeTodaySuccessCount(todayStart),
            outboxDao.observeTodayFailureCount(todayStart),
        ) { pending, lastSuccess, lastFailure, successCount, failureCount ->
            DashboardState(
                pendingCount = pending,
                lastSuccessAt = lastSuccess,
                lastFailureReason = lastFailure,
                todaySuccessCount = successCount,
                todayFailureCount = failureCount,
            )
        }
    }

    override suspend fun getLastAcceptedAtForDedupe(packageName: String, title: String?, text: String?): Long? {
        return notificationEventDao.findLastAcceptedAt(packageName, title, text)
    }

    override suspend fun clearLogs() {
        deliveryAttemptDao.clearAll()
        outboxDao.clearAll()
    }

    override suspend fun exportDebugSnapshot(): String {
        val events = notificationEventDao.getAllNow()
        val outboxes = outboxDao.getAllNow()
        val attempts = deliveryAttemptDao.getAllNow()
        return buildString {
            appendLine("NotifyBridge debug export")
            appendLine("generatedAt=${System.currentTimeMillis()}")
            appendLine("events=${events.size}")
            events.forEach { appendLine("event=${it.eventId}, package=${it.packageName}, title=${it.title}") }
            appendLine("outbox=${outboxes.size}")
            outboxes.forEach { appendLine("outbox=${it.eventId}, status=${it.status}, attempt=${it.attemptCount}, error=${it.errorMessage}") }
            appendLine("attempts=${attempts.size}")
            attempts.forEach { appendLine("attempt=${it.eventId}, code=${it.responseCode}, error=${it.errorMessage}") }
        }
    }
}
