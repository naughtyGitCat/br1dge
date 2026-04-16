package com.example.notifybridge.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notifybridge.core.database.entity.DeliveryAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DeliveryAttemptEntity)

    @Query("SELECT * FROM delivery_attempts WHERE eventId = :eventId ORDER BY createdAt DESC")
    suspend fun getByEventId(eventId: String): List<DeliveryAttemptEntity>

    @Query("SELECT * FROM delivery_attempts WHERE eventId = :eventId ORDER BY createdAt DESC")
    fun observeByEventId(eventId: String): Flow<List<DeliveryAttemptEntity>>

    @Query("SELECT * FROM delivery_attempts ORDER BY createdAt DESC")
    suspend fun getAllNow(): List<DeliveryAttemptEntity>

    @Query("DELETE FROM delivery_attempts")
    suspend fun clearAll()
}
