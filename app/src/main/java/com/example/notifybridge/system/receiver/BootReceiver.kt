package com.example.notifybridge.system.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notifybridge.system.util.DeliveryWorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var deliveryWorkScheduler: DeliveryWorkScheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            deliveryWorkScheduler.enqueueNow()
        }
    }
}
