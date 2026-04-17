package com.example.notifybridge.domain.model

data class DashboardState(
    val listenerEnabled: Boolean = false,
    val lastSuccessAt: Long? = null,
    val lastFailureReason: String? = null,
    val nextRetryAt: Long? = null,
    val pendingCount: Int = 0,
    val todaySuccessCount: Int = 0,
    val todayFailureCount: Int = 0,
)
