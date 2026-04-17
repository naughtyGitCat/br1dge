package com.example.notifybridge.domain.usecase

import android.os.Build
import com.example.notifybridge.core.network.toJson
import com.example.notifybridge.domain.model.DeliveryStatus
import com.example.notifybridge.domain.model.ForwardError
import com.example.notifybridge.domain.model.ForwardPayload
import com.example.notifybridge.domain.model.ForwardResult
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import com.example.notifybridge.system.util.NotificationCancellationCoordinator
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SendPendingNotificationsUseCase @Inject constructor(
    private val deliveryLogRepository: DeliveryLogRepository,
    private val forwardRepository: ForwardRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCancellationCoordinator: NotificationCancellationCoordinator,
) {
    suspend operator fun invoke(): DeliveryRunSummary = coroutineScope {
        val settings = settingsRepository.getSettings()
        val autoRetryEnabled = settings.filterRuleSet.autoRetryEnabled
        var success = 0
        var retrying = 0
        var failed = 0

        deliveryLogRepository.getPendingEvents().forEach { event ->
            deliveryLogRepository.markSending(event.eventId)
            val payload = ForwardPayload(
                appPackage = event.packageName,
                appName = event.appName,
                title = event.title,
                text = event.text,
                subText = event.subText,
                postTime = event.postTime,
                receivedAt = event.receivedAt,
                deviceModel = Build.MODEL ?: "Unknown",
                androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            )
            when (val result = forwardRepository.sendPayload(payload)) {
                is ForwardResult.Success -> {
                    deliveryLogRepository.appendAttempt(
                        eventId = event.eventId,
                        errorMessage = null,
                        responseCode = 200,
                        payloadJson = payload.toJson(),
                    )
                    deliveryLogRepository.markDelivered(event.eventId)
                    if (settings.cancelNotificationOnSuccess && event.clearable) {
                        notificationCancellationCoordinator.cancelNotification(event.notificationKey)
                    }
                    success++
                }
                is ForwardResult.Failure -> {
                    deliveryLogRepository.appendAttempt(
                        eventId = event.eventId,
                        errorMessage = result.error.message,
                        responseCode = (result.error as? ForwardError.HttpError)?.code,
                        payloadJson = payload.toJson(),
                    )
                    val shouldRetry = result.error.retryable
                    deliveryLogRepository.markFailed(
                        eventId = event.eventId,
                        errorMessage = result.error.message,
                        retrying = shouldRetry && autoRetryEnabled,
                        responseCode = (result.error as? ForwardError.HttpError)?.code,
                    )
                    if (shouldRetry && autoRetryEnabled) {
                        retrying++
                    } else {
                        failed++
                    }
                }
            }
        }

        DeliveryRunSummary(
            success = success,
            retrying = retrying,
            failed = failed,
            nextRetryAt = deliveryLogRepository.getNextRetryAt(),
        )
    }
}

data class DeliveryRunSummary(
    val success: Int,
    val retrying: Int,
    val failed: Int,
    val nextRetryAt: Long?,
)
