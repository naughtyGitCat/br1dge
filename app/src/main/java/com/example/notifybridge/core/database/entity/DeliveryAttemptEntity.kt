package com.example.notifybridge.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_attempts")
data class DeliveryAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val payloadJson: String,
    val responseCode: Int?,
    val errorMessage: String?,
    val createdAt: Long,
)
