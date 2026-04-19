package com.example.notifybridge.core.common

import java.util.Locale

object NotifyBridgeStrings {
    fun commonDueSoon(): String = if (Locale.getDefault().language.startsWith("zh")) "即将执行" else "Due soon"

    fun afterMinSec(minutes: Long, seconds: Long): String =
        if (Locale.getDefault().language.startsWith("zh")) {
            "${minutes}分${seconds}秒后"
        } else {
            "${minutes} min ${seconds} sec"
        }

    fun afterMinutes(minutes: Long): String =
        if (Locale.getDefault().language.startsWith("zh")) {
            "${minutes}分钟后"
        } else {
            "${minutes} min"
        }

    fun afterSeconds(seconds: Long): String =
        if (Locale.getDefault().language.startsWith("zh")) {
            "${seconds}秒后"
        } else {
            "${seconds} sec"
        }
}
