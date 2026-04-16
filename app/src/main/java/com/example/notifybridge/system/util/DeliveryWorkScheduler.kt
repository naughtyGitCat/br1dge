package com.example.notifybridge.system.util

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notifybridge.system.worker.DeliveryWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueueNow() {
        val request = OneTimeWorkRequestBuilder<DeliveryWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            DeliveryWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.APPEND,
            request,
        )
    }
}
