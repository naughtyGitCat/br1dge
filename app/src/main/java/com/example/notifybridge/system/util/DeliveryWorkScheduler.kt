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
import kotlin.math.max
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueueAutomatic() {
        enqueueImmediate(
            uniqueWorkName = DeliveryWorker.IMMEDIATE_WORK_NAME,
            policy = ExistingWorkPolicy.KEEP,
        )
    }

    fun enqueueUserInitiated() {
        enqueueImmediate(
            uniqueWorkName = DeliveryWorker.IMMEDIATE_WORK_NAME,
            policy = ExistingWorkPolicy.APPEND_OR_REPLACE,
        )
    }

    fun enqueueAutomaticRetry(nextRetryAt: Long) {
        val delayMillis = max(0L, nextRetryAt - System.currentTimeMillis())
        val request = newRequest(delayMillis = delayMillis)
        WorkManager.getInstance(context).enqueueUniqueWork(
            DeliveryWorker.RETRY_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelAutomaticRetry() {
        WorkManager.getInstance(context).cancelUniqueWork(DeliveryWorker.RETRY_WORK_NAME)
    }

    private fun enqueueImmediate(
        uniqueWorkName: String,
        policy: ExistingWorkPolicy,
    ) {
        val request = newRequest(delayMillis = 0L)
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName,
            policy,
            request,
        )
    }

    private fun newRequest(delayMillis: Long) =
        OneTimeWorkRequestBuilder<DeliveryWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
}
