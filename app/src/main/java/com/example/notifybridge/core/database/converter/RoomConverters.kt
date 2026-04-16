package com.example.notifybridge.core.database.converter

import androidx.room.TypeConverter
import com.example.notifybridge.domain.model.DeliveryStatus

class RoomConverters {
    @TypeConverter
    fun fromStatus(value: DeliveryStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): DeliveryStatus = DeliveryStatus.valueOf(value)
}
