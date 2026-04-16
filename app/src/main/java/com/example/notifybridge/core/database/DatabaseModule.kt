package com.example.notifybridge.core.database

import android.content.Context
import androidx.room.Room
import com.example.notifybridge.core.database.dao.DeliveryAttemptDao
import com.example.notifybridge.core.database.dao.NotificationEventDao
import com.example.notifybridge.core.database.dao.OutboxDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotifyBridgeDatabase {
        return Room.databaseBuilder(context, NotifyBridgeDatabase::class.java, NotifyBridgeDatabase.DATABASE_NAME)
            .addMigrations(*NotifyBridgeDatabase.migrations)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideNotificationEventDao(database: NotifyBridgeDatabase): NotificationEventDao = database.notificationEventDao()

    @Provides
    fun provideOutboxDao(database: NotifyBridgeDatabase): OutboxDao = database.outboxDao()

    @Provides
    fun provideDeliveryAttemptDao(database: NotifyBridgeDatabase): DeliveryAttemptDao = database.deliveryAttemptDao()
}
