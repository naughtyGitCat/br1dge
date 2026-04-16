package com.example.notifybridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.notifybridge.core.database.entity.OutboxEntity
import com.example.notifybridge.domain.model.DeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity): Long

    @Query("UPDATE outbox SET status = :status, updatedAt = :updatedAt WHERE eventId = :eventId")
    suspend fun updateStatus(eventId: String, status: DeliveryStatus, updatedAt: Long)

    @Query(
        """
        UPDATE outbox
        SET status = :status, attemptCount = attemptCount + 1, errorMessage = :errorMessage,
            responseCode = :responseCode, updatedAt = :updatedAt
        WHERE eventId = :eventId
        """
    )
    suspend fun markFailure(
        eventId: String,
        status: DeliveryStatus,
        errorMessage: String,
        responseCode: Int?,
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

    @Query("SELECT COUNT(*) FROM outbox WHERE status IN ('PENDING', 'RETRYING')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM outbox ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM outbox WHERE status = :status ORDER BY updatedAt DESC")
    fun observeByStatus(status: DeliveryStatus): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM outbox WHERE eventId = :eventId LIMIT 1")
    fun observeByEventId(eventId: String): Flow<OutboxEntity?>

    @Query("SELECT * FROM outbox ORDER BY updatedAt DESC")
    suspend fun getAllNow(): List<OutboxEntity>

    @Query("DELETE FROM outbox")
    suspend fun clearAll()

    @Query("SELECT MAX(updatedAt) FROM outbox WHERE status = 'SUCCESS'")
    fun observeLastSuccessAt(): Flow<Long?>

    @Query("SELECT errorMessage FROM outbox WHERE status = 'FAILED' ORDER BY updatedAt DESC LIMIT 1")
    fun observeLastFailureReason(): Flow<String?>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'SUCCESS' AND updatedAt >= :startOfDay")
    fun observeTodaySuccessCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'FAILED' AND updatedAt >= :startOfDay")
    fun observeTodayFailureCount(startOfDay: Long): Flow<Int>
}
