package com.example.notifybridge.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notifybridge.core.database.converter.RoomConverters
import com.example.notifybridge.core.database.dao.DeliveryAttemptDao
import com.example.notifybridge.core.database.dao.NotificationEventDao
import com.example.notifybridge.core.database.dao.OutboxDao
import com.example.notifybridge.core.database.entity.DeliveryAttemptEntity
import com.example.notifybridge.core.database.entity.NotificationEventEntity
import com.example.notifybridge.core.database.entity.OutboxEntity

@Database(
    entities = [
        NotificationEventEntity::class,
        OutboxEntity::class,
        DeliveryAttemptEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class NotifyBridgeDatabase : RoomDatabase() {
    abstract fun notificationEventDao(): NotificationEventDao
    abstract fun outboxDao(): OutboxDao
    abstract fun deliveryAttemptDao(): DeliveryAttemptDao

    companion object {
        const val DATABASE_NAME = "notifybridge.db"

        /**
         * Room migration 预留点。
         * 当前 MVP 为 v1，没有实际迁移逻辑；后续升级时在这里追加 Migration 对象。
         */
        val migrations: Array<Migration> = arrayOf(
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    // 预留示例：升级到 v2 时在这里执行 ALTER TABLE。
                }
            }
        )
    }
}
