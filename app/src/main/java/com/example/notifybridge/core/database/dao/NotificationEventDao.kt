package com.example.notifybridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notifybridge.core.database.entity.NotificationEventEntity

@Dao
interface NotificationEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationEventEntity)

    @Query(
        """
        SELECT receivedAt FROM notification_events
        WHERE packageName = :packageName
        AND IFNULL(title, '') = IFNULL(:title, '')
        AND IFNULL(text, '') = IFNULL(:text, '')
        ORDER BY receivedAt DESC
        LIMIT 1
        """
    )
    suspend fun findLastAcceptedAt(packageName: String, title: String?, text: String?): Long?

    @Query("SELECT * FROM notification_events WHERE eventId = :eventId LIMIT 1")
    suspend fun getById(eventId: String): NotificationEventEntity?

    @Query("SELECT * FROM notification_events ORDER BY receivedAt DESC")
    suspend fun getAllNow(): List<NotificationEventEntity>

    @Query(
        """
        SELECT n.* FROM notification_events n
        INNER JOIN outbox o ON o.eventId = n.eventId
        WHERE o.status IN ('PENDING', 'RETRYING')
        ORDER BY o.updatedAt ASC
        LIMIT :limit
        """
    )
    suspend fun getPendingEvents(limit: Int): List<NotificationEventEntity>
}
