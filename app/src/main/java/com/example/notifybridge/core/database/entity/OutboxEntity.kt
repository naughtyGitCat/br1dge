package com.example.notifybridge.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notifybridge.domain.model.DeliveryStatus

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val status: DeliveryStatus,
    val attemptCount: Int,
    val errorMessage: String?,
    val responseCode: Int?,
    val nextRetryAt: Long?,
    val payloadJson: String,
    val createdAt: Long,
    val updatedAt: Long,
)
