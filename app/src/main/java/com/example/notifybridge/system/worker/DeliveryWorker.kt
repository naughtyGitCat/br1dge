package com.example.notifybridge.system.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notifybridge.domain.usecase.SendPendingNotificationsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeliveryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sendPendingNotificationsUseCase: SendPendingNotificationsUseCase,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val summary = sendPendingNotificationsUseCase()
        return when {
            summary.retrying > 0 -> Result.retry()
            summary.failed > 0 && summary.success == 0 -> Result.failure()
            else -> Result.success()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "notifybridge_delivery"
    }
}
