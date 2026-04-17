package com.example.notifybridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.notifybridge.core.database.entity.DeliveryLogRow
import com.example.notifybridge.core.database.entity.OutboxEntity
import com.example.notifybridge.domain.model.DeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity): Long

    @Query(
        """
        UPDATE outbox
        SET status = :status, nextRetryAt = :nextRetryAt, updatedAt = :updatedAt
        WHERE eventId = :eventId
        """
    )
    suspend fun updateStatus(
        eventId: String,
        status: DeliveryStatus,
        nextRetryAt: Long?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE outbox
        SET status = :status, attemptCount = attemptCount + 1, errorMessage = :errorMessage,
            responseCode = :responseCode, nextRetryAt = :nextRetryAt, updatedAt = :updatedAt
        WHERE eventId = :eventId
        """
    )
    suspend fun markFailure(
        eventId: String,
        status: DeliveryStatus,
        errorMessage: String,
        responseCode: Int?,
        nextRetryAt: Long?,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE outbox
        SET payloadJson = :payloadJson, updatedAt = :updatedAt
        WHERE eventId = :eventId
        """
    )
    suspend fun updatePayloadSnapshot(eventId: String, payloadJson: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'RETRYING'")
    fun observeRetryingCount(): Flow<Int>

    @Query("SELECT * FROM outbox ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM outbox WHERE status = :status ORDER BY updatedAt DESC")
    fun observeByStatus(status: DeliveryStatus): Flow<List<OutboxEntity>>

    @Query(
        """
        SELECT
            o.eventId AS eventId,
            n.appName AS appName,
            n.title AS title,
            n.text AS text,
            o.status AS status,
            o.attemptCount AS attemptCount,
            o.errorMessage AS errorMessage,
            o.createdAt AS createdAt,
            o.updatedAt AS updatedAt,
            o.nextRetryAt AS nextRetryAt,
            o.payloadJson AS payloadJson,
            o.responseCode AS responseCode
        FROM outbox o
        INNER JOIN notification_events n ON n.eventId = o.eventId
        WHERE (:status IS NULL OR o.status = :status)
          AND (
            :query = '' OR
            n.appName LIKE '%' || :query || '%' OR
            IFNULL(n.title, '') LIKE '%' || :query || '%' OR
            IFNULL(n.text, '') LIKE '%' || :query || '%' OR
            IFNULL(o.errorMessage, '') LIKE '%' || :query || '%' OR
            o.status LIKE '%' || :query || '%'
          )
        ORDER BY o.updatedAt DESC
        LIMIT :limit
        """
    )
    fun observeLogPage(
        status: String?,
        query: String,
        limit: Int,
    ): Flow<List<DeliveryLogRow>>

    @Query(
        """
        SELECT COUNT(*)
        FROM outbox o
        INNER JOIN notification_events n ON n.eventId = o.eventId
        WHERE (:status IS NULL OR o.status = :status)
          AND (
            :query = '' OR
            n.appName LIKE '%' || :query || '%' OR
            IFNULL(n.title, '') LIKE '%' || :query || '%' OR
            IFNULL(n.text, '') LIKE '%' || :query || '%' OR
            IFNULL(o.errorMessage, '') LIKE '%' || :query || '%' OR
            o.status LIKE '%' || :query || '%'
          )
        """
    )
    fun observeLogCount(
        status: String?,
        query: String,
    ): Flow<Int>

    @Query("SELECT * FROM outbox WHERE eventId = :eventId LIMIT 1")
    fun observeByEventId(eventId: String): Flow<OutboxEntity?>

    @Query("SELECT * FROM outbox WHERE eventId = :eventId LIMIT 1")
    suspend fun getByEventId(eventId: String): OutboxEntity?

    @Query("SELECT * FROM outbox ORDER BY updatedAt DESC")
    suspend fun getAllNow(): List<OutboxEntity>

    @Query("DELETE FROM outbox")
    suspend fun clearAll()

    @Query("SELECT MAX(updatedAt) FROM outbox WHERE status = 'SUCCESS'")
    fun observeLastSuccessAt(): Flow<Long?>

    @Query("SELECT errorMessage FROM outbox WHERE status = 'FAILED' ORDER BY updatedAt DESC LIMIT 1")
    fun observeLastFailureReason(): Flow<String?>

    @Query("SELECT MIN(nextRetryAt) FROM outbox WHERE status = 'RETRYING' AND nextRetryAt IS NOT NULL")
    fun observeNextRetryAt(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'SUCCESS' AND updatedAt >= :startOfDay")
    fun observeTodaySuccessCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'FAILED' AND updatedAt >= :startOfDay")
    fun observeTodayFailureCount(startOfDay: Long): Flow<Int>
}
