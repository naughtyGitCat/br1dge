package com.example.notifybridge.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notifybridge.core.datastore.SecureSettingsStore
import com.example.notifybridge.core.datastore.SettingsDataStore
import com.example.notifybridge.system.util.SettingsBackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.concurrent.thread

class DebugSettingsExportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_EXPORT) return

        val pendingResult = goAsync()
        thread(name = "notifybridge-debug-export") {
            runCatching {
                val appContext = context.applicationContext
                val settings = runBlocking {
                    SettingsDataStore(
                        context = appContext,
                        secureSettingsStore = SecureSettingsStore(appContext),
                    ).observeSettings().first()
                }
                val json = SettingsBackupManager(appContext).encode(settings)
                val exportDir = requireNotNull(appContext.getExternalFilesDir(null)) {
                    "External files directory is unavailable"
                }
                val file = File(exportDir, EXPORT_FILE_NAME)
                file.writeText(json)
                Log.i(TAG, "Exported settings backup to ${file.absolutePath}")
            }.onFailure { throwable ->
                Log.e(TAG, "Failed to export settings backup", throwable)
            }
            pendingResult.finish()
        }
    }

    private companion object {
        const val ACTION_EXPORT = "com.example.notifybridge.DEBUG_EXPORT_SETTINGS"
        const val EXPORT_FILE_NAME = "notifybridge-settings-backup.json"
        const val TAG = "NBSettingsExport"
    }
}
