package com.example.notifybridge.domain.usecase

import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.NotificationEvent
import javax.inject.Inject

sealed class FilterDecision {
    data object Allowed : FilterDecision()
    data class Blocked(val reason: String) : FilterDecision()
}

class ShouldForwardNotificationUseCase @Inject constructor() {

    operator fun invoke(
        event: NotificationEvent,
        settings: AppSettings,
        lastAcceptedAt: Long?,
    ): FilterDecision {
        if (!settings.forwardingEnabled) {
            return FilterDecision.Blocked("转发总开关未开启")
        }

        val ruleSet = settings.filterRuleSet
        if (!ruleSet.enabled) {
            return FilterDecision.Allowed
        }
        if (ruleSet.excludeSystemNotifications && event.isSystemNotification) {
            return FilterDecision.Blocked("系统通知已排除")
        }
        if (ruleSet.excludeOngoingNotifications && event.ongoing) {
            return FilterDecision.Blocked("ongoing 通知已排除")
        }
        if (ruleSet.excludeEmptyTextNotifications && event.title.isNullOrBlank() && event.text.isNullOrBlank()) {
            return FilterDecision.Blocked("空正文通知已排除")
        }
        if (ruleSet.blockedPackages.contains(event.packageName)) {
            return FilterDecision.Blocked("命中包名黑名单")
        }
        if (ruleSet.allowedPackages.isNotEmpty() && !ruleSet.allowedPackages.contains(event.packageName)) {
            return FilterDecision.Blocked("未命中包名白名单")
        }

        val searchableText = buildString {
            append(event.title.orEmpty())
            append(' ')
            append(event.text.orEmpty())
            append(' ')
            append(event.subText.orEmpty())
        }.trim()

        if (ruleSet.keywordBlacklist.any { keyword -> searchableText.contains(keyword, ignoreCase = true) }) {
            return FilterDecision.Blocked("命中关键词黑名单")
        }
        if (ruleSet.keywordWhitelist.isNotEmpty() &&
            ruleSet.keywordWhitelist.none { keyword -> searchableText.contains(keyword, ignoreCase = true) }
        ) {
            return FilterDecision.Blocked("未命中关键词白名单")
        }

        val dedupeWindowMillis = ruleSet.dedupeWindowSeconds * 1000L
        if (lastAcceptedAt != null && event.receivedAt - lastAcceptedAt < dedupeWindowMillis) {
            return FilterDecision.Blocked("命中去重时间窗口")
        }
        return FilterDecision.Allowed
    }
}
