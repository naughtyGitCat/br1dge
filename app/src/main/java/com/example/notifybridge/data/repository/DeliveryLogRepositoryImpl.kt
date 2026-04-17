package com.example.notifybridge.data.repository

import com.example.notifybridge.data.mapper.toEntity
import com.example.notifybridge.core.database.dao.DeliveryAttemptDao
import com.example.notifybridge.core.database.dao.NotificationEventDao
import com.example.notifybridge.core.database.dao.OutboxDao
import com.example.notifybridge.core.database.entity.DeliveryAttemptEntity
import com.example.notifybridge.core.database.entity.OutboxEntity
import com.example.notifybridge.core.network.appJson
import com.example.notifybridge.domain.model.DashboardState
import com.example.notifybridge.domain.model.DeliveryAttempt
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.DeliveryStatus
import com.example.notifybridge.domain.model.NotificationEvent
import com.example.notifybridge.domain.model.RetrySchedule
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
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
                nextRetryAt = null,
                payloadJson = "",
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun getPendingEvents(limit: Int): List<NotificationEvent> {
        return notificationEventDao.getPendingEvents(limit).map { entity ->
            NotificationEvent(
                eventId = entity.eventId,
                packageName = entity.packageName,
                appName = entity.appName,
                title = entity.title,
                text = entity.text,
                subText = entity.subText,
                postTime = entity.postTime,
                notificationKey = entity.notificationKey,
                ongoing = entity.ongoing,
                clearable = entity.clearable,
                isSystemNotification = entity.isSystemNotification,
                receivedAt = entity.receivedAt,
            )
        }
    }

    override suspend fun markSending(eventId: String) {
        outboxDao.updateStatus(
            eventId = eventId,
            status = DeliveryStatus.RETRYING,
            nextRetryAt = null,
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun markDelivered(eventId: String) {
        outboxDao.updateStatus(
            eventId = eventId,
            status = DeliveryStatus.SUCCESS,
            nextRetryAt = null,
            updatedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun markFailed(eventId: String, errorMessage: String, retrying: Boolean, responseCode: Int?) {
        val now = System.currentTimeMillis()
        val currentAttemptCount = outboxDao.getByEventId(eventId)?.attemptCount ?: 0
        val nextRetryAt = if (retrying) {
            RetrySchedule.nextRetryAt(
                baseTimeMillis = now,
                completedAttempts = currentAttemptCount + 1,
            )
        } else {
            null
        }
        outboxDao.markFailure(
            eventId = eventId,
            status = if (retrying) DeliveryStatus.RETRYING else DeliveryStatus.FAILED,
            errorMessage = errorMessage,
            responseCode = responseCode,
            nextRetryAt = nextRetryAt,
            updatedAt = now,
        )
    }

    override suspend fun markPending(eventId: String) {
        outboxDao.updateStatus(
            eventId = eventId,
            status = DeliveryStatus.PENDING,
            nextRetryAt = null,
            updatedAt = System.currentTimeMillis(),
        )
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

    override fun observeLogs(status: DeliveryStatus?, query: String, limit: Int): Flow<List<DeliveryRecord>> {
        return outboxDao.observeLogPage(
            status = status?.name,
            query = query.trim(),
            limit = limit,
        ).map { rows ->
            rows.map { row ->
                DeliveryRecord(
                    eventId = row.eventId,
                    appName = row.appName,
                    title = row.title,
                    text = row.text,
                    status = DeliveryStatus.valueOf(row.status),
                    attemptCount = row.attemptCount,
                    errorMessage = row.errorMessage,
                    createdAt = row.createdAt,
                    updatedAt = row.updatedAt,
                    nextRetryAt = row.nextRetryAt,
                    payloadJson = row.payloadJson,
                    responseCode = row.responseCode,
                )
            }
        }
    }

    override fun observeLogCount(status: DeliveryStatus?, query: String): Flow<Int> {
        return outboxDao.observeLogCount(
            status = status?.name,
            query = query.trim(),
        )
    }

    override fun observeDetail(eventId: String): Flow<DeliveryRecord?> {
        return outboxDao.observeByEventId(eventId).map { outbox ->
            val entity = outbox ?: return@map null
            val event = notificationEventDao.getById(entity.eventId) ?: return@map null
            DeliveryRecord(
                eventId = entity.eventId,
                appName = event.appName,
                title = event.title,
                text = event.text,
                status = entity.status,
                attemptCount = entity.attemptCount,
                errorMessage = entity.errorMessage,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                nextRetryAt = entity.nextRetryAt,
                payloadJson = entity.payloadJson,
                responseCode = entity.responseCode,
            )
        }
    }

    override fun observeNotificationEvent(eventId: String): Flow<NotificationEvent?> {
        return outboxDao.observeByEventId(eventId).map { outbox ->
            val resolvedEventId = outbox?.eventId ?: eventId
            val entity = notificationEventDao.getById(resolvedEventId) ?: return@map null
            NotificationEvent(
                eventId = entity.eventId,
                packageName = entity.packageName,
                appName = entity.appName,
                title = entity.title,
                text = entity.text,
                subText = entity.subText,
                postTime = entity.postTime,
                notificationKey = entity.notificationKey,
                ongoing = entity.ongoing,
                clearable = entity.clearable,
                isSystemNotification = entity.isSystemNotification,
                receivedAt = entity.receivedAt,
            )
        }
    }

    override fun observeAttempts(eventId: String): Flow<List<DeliveryAttempt>> {
        return deliveryAttemptDao.observeByEventId(eventId).map { attempts ->
            attempts.map {
                DeliveryAttempt(
                    id = it.id,
                    eventId = it.eventId,
                    payloadJson = it.payloadJson,
                    responseCode = it.responseCode,
                    errorMessage = it.errorMessage,
                    createdAt = it.createdAt,
                )
            }
        }
    }

    override fun observeDashboardState(): Flow<DashboardState> {
        val todayStart = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        return combine(
            combine(
                outboxDao.observePendingCount(),
                outboxDao.observeRetryingCount(),
                outboxDao.observeLastSuccessAt(),
                outboxDao.observeLastFailureReason(),
            ) { pending, retryingCount, lastSuccess, lastFailure ->
                DashboardLeft(
                    pending = pending,
                    retryingCount = retryingCount,
                    lastSuccess = lastSuccess,
                    lastFailure = lastFailure,
                )
            },
            combine(
                outboxDao.observeNextRetryAt(),
                outboxDao.observeTodaySuccessCount(todayStart),
                outboxDao.observeTodayFailureCount(todayStart),
            ) { nextRetryAt, successCount, failureCount ->
                Triple(nextRetryAt, successCount, failureCount)
            },
        ) { left, right ->
            val pending = left.pending
            val retryingCount = left.retryingCount
            val lastSuccess = left.lastSuccess
            val lastFailure = left.lastFailure
            val (nextRetryAt, successCount, failureCount) = right
            DashboardState(
                pendingCount = pending,
                retryingCount = retryingCount,
                lastSuccessAt = lastSuccess,
                lastFailureReason = lastFailure,
                nextRetryAt = nextRetryAt,
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
        return appJson.encodeToString(
            DebugSnapshot(
                generatedAt = System.currentTimeMillis(),
                events = events.map {
                    DebugEvent(
                        eventId = it.eventId,
                        packageName = it.packageName,
                        appName = it.appName,
                        title = it.title,
                        text = it.text,
                        receivedAt = it.receivedAt,
                    )
                },
                outboxes = outboxes.map {
                    DebugOutbox(
                        eventId = it.eventId,
                        status = it.status.name,
                        attemptCount = it.attemptCount,
                        errorMessage = it.errorMessage,
                        responseCode = it.responseCode,
                        nextRetryAt = it.nextRetryAt,
                        updatedAt = it.updatedAt,
                    )
                },
                attempts = attempts.map {
                    DebugAttempt(
                        eventId = it.eventId,
                        responseCode = it.responseCode,
                        errorMessage = it.errorMessage,
                        createdAt = it.createdAt,
                    )
                }
            )
        )
    }
}

@Serializable
private data class DebugSnapshot(
    val generatedAt: Long,
    val events: List<DebugEvent>,
    val outboxes: List<DebugOutbox>,
    val attempts: List<DebugAttempt>,
)

@Serializable
private data class DebugEvent(
    val eventId: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val receivedAt: Long,
)

@Serializable
private data class DebugOutbox(
    val eventId: String,
    val status: String,
    val attemptCount: Int,
    val errorMessage: String?,
    val responseCode: Int?,
    val nextRetryAt: Long?,
    val updatedAt: Long,
)

private data class DashboardLeft(
    val pending: Int,
    val retryingCount: Int,
    val lastSuccess: Long?,
    val lastFailure: String?,
)

@Serializable
private data class DebugAttempt(
    val eventId: String,
    val responseCode: Int?,
    val errorMessage: String?,
    val createdAt: Long,
)
