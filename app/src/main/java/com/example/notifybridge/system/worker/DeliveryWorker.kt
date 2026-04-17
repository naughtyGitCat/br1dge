package com.example.notifybridge.system.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notifybridge.domain.usecase.SendPendingNotificationsUseCase
import com.example.notifybridge.system.util.DeliveryWorkScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeliveryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sendPendingNotificationsUseCase: SendPendingNotificationsUseCase,
    private val deliveryWorkScheduler: DeliveryWorkScheduler,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val summary = sendPendingNotificationsUseCase()
        summary.nextRetryAt?.let(deliveryWorkScheduler::enqueueAutomaticRetry)
            ?: deliveryWorkScheduler.cancelAutomaticRetry()
        return when {
            summary.retrying > 0 -> Result.success()
            summary.failed > 0 && summary.success == 0 -> Result.failure()
            else -> Result.success()
        }
    }

    companion object {
        const val IMMEDIATE_WORK_NAME = "notifybridge_delivery_immediate"
        const val RETRY_WORK_NAME = "notifybridge_delivery_retry"
    }
}
