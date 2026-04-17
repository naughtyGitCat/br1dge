package com.example.notifybridge.domain.model

object RetrySchedule {
    private val retryDelaysMillis = listOf(
        30_000L,
        60_000L,
        120_000L,
        240_000L,
        480_000L,
        960_000L,
    )

    fun nextRetryAt(baseTimeMillis: Long, completedAttempts: Int): Long {
        val delay = retryDelaysMillis.getOrElse((completedAttempts - 1).coerceAtLeast(0)) {
            retryDelaysMillis.last()
        }
        return baseTimeMillis + delay
    }
}
