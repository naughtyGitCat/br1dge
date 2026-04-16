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
                DeliveryRecord(
                    eventId = outbox.eventId,
                    appName = event.appName,
                    title = event.title,
                    text = event.text,
                    status = outbox.status,
                    attemptCount = outbox.attemptCount,
                    errorMessage = outbox.errorMessage,
                    createdAt = outbox.createdAt,
                    updatedAt = outbox.updatedAt,
                    payloadJson = outbox.payloadJson,
                    responseCode = outbox.responseCode,
                )
            }
        }
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
                payloadJson = entity.payloadJson,
                responseCode = entity.responseCode,
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
    val updatedAt: Long,
)

@Serializable
private data class DebugAttempt(
    val eventId: String,
    val responseCode: Int?,
    val errorMessage: String?,
    val createdAt: Long,
)
