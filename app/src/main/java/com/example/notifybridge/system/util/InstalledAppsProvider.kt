package com.example.notifybridge.system.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
)

@Singleton
class InstalledAppsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Suppress("DEPRECATION")
    fun getInstalledApps(): List<InstalledAppInfo> {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != context.packageName }
            .map { appInfo ->
                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo)
                        .toString()
                        .takeIf { it.isNotBlank() }
                        ?: appInfo.packageName,
                    isSystemApp = appInfo.isSystemApp(),
                )
            }
            .sortedWith(
                compareBy<InstalledAppInfo> { it.isSystemApp }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.appName }
                    .thenBy { it.packageName },
            )
            .toList()
    }

    private fun ApplicationInfo.isSystemApp(): Boolean {
        return flags and ApplicationInfo.FLAG_SYSTEM != 0 ||
            flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
    }
}
