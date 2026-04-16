package com.example.notifybridge.system.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun exportText(content: String): File {
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val file = File(dir, "notifybridge-debug-${System.currentTimeMillis()}.txt")
        file.writeText(content)
        return file
    }
}
