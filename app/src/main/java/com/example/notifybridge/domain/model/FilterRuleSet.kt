package com.example.notifybridge.domain.model

data class FilterRuleSet(
    val enabled: Boolean = false,
    val allowedPackages: Set<String> = emptySet(),
    val blockedPackages: Set<String> = emptySet(),
    val keywordWhitelist: Set<String> = emptySet(),
    val keywordBlacklist: Set<String> = emptySet(),
    val excludeSystemNotifications: Boolean = true,
    val excludeOngoingNotifications: Boolean = false,
    val excludeEmptyTextNotifications: Boolean = false,
    val dedupeWindowSeconds: Int = 10,
    val autoRetryEnabled: Boolean = true,
)
