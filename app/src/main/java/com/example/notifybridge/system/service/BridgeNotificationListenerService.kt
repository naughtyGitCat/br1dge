package com.example.notifybridge.system.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.notifybridge.core.common.AppDispatchers
import com.example.notifybridge.domain.usecase.HandleIncomingNotificationUseCase
import com.example.notifybridge.system.util.DeliveryWorkScheduler
import com.example.notifybridge.system.util.NotificationCancellationCoordinator
import com.example.notifybridge.system.util.NotificationParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BridgeNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var dispatchers: AppDispatchers

    @Inject
    lateinit var notificationParser: NotificationParser

    @Inject
    lateinit var handleIncomingNotificationUseCase: HandleIncomingNotificationUseCase

    @Inject
    lateinit var deliveryWorkScheduler: DeliveryWorkScheduler

    @Inject
    lateinit var notificationCancellationCoordinator: NotificationCancellationCoordinator

    private val serviceScope by lazy {
        CoroutineScope(SupervisorJob() + dispatchers.io)
    }

    override fun onCreate() {
        super.onCreate()
        notificationCancellationCoordinator.attach(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        serviceScope.launch {
            val event = notificationParser.parse(this@BridgeNotificationListenerService, notification)
            when (handleIncomingNotificationUseCase(event)) {
                is com.example.notifybridge.domain.usecase.HandleIncomingResult.Enqueued -> {
                    deliveryWorkScheduler.enqueueAutomatic()
                }
                is com.example.notifybridge.domain.usecase.HandleIncomingResult.Ignored -> Unit
            }
        }
    }

    override fun onDestroy() {
        notificationCancellationCoordinator.detach(this)
        serviceScope.cancel()
        super.onDestroy()
    }
}
