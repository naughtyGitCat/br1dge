package com.example.notifybridge.domain.usecase

import com.example.notifybridge.core.common.LocalizedText
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.DeliveryChannel
import com.example.notifybridge.domain.model.NotificationEvent
import javax.inject.Inject

sealed class FilterDecision {
    data object Allowed : FilterDecision()
    data class Blocked(val reason: String) : FilterDecision()
}

class ShouldForwardNotificationUseCase @Inject constructor() {

    private val telegramPackages = setOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.telegram.plus",
    )

    private val slackPackages = setOf(
        "com.Slack",
    )

    operator fun invoke(
        event: NotificationEvent,
        settings: AppSettings,
        lastAcceptedAt: Long?,
    ): FilterDecision {
        if (!settings.forwardingEnabled) {
            return FilterDecision.Blocked(LocalizedText.forwardingDisabled())
        }

        detectChannelLoop(event.packageName, settings.deliveryChannel)?.let { reason ->
            return FilterDecision.Blocked(reason)
        }

        val ruleSet = settings.filterRuleSet
        if (!ruleSet.enabled) {
            return FilterDecision.Allowed
        }
        if (ruleSet.excludeSystemNotifications && event.isSystemNotification) {
            return FilterDecision.Blocked(LocalizedText.systemExcluded())
        }
        if (ruleSet.excludeOngoingNotifications && event.ongoing) {
            return FilterDecision.Blocked(LocalizedText.ongoingExcluded())
        }
        if (ruleSet.excludeEmptyTextNotifications && event.title.isNullOrBlank() && event.text.isNullOrBlank()) {
            return FilterDecision.Blocked(LocalizedText.emptyBodyExcluded())
        }
        if (ruleSet.blockedPackages.contains(event.packageName)) {
            return FilterDecision.Blocked(LocalizedText.packageBlacklistHit())
        }
        if (ruleSet.allowedPackages.isNotEmpty() && !ruleSet.allowedPackages.contains(event.packageName)) {
            return FilterDecision.Blocked(LocalizedText.packageWhitelistMiss())
        }

        val searchableText = buildString {
            append(event.title.orEmpty())
            append(' ')
            append(event.text.orEmpty())
            append(' ')
            append(event.subText.orEmpty())
        }.trim()

        if (ruleSet.keywordBlacklist.any { keyword -> searchableText.contains(keyword, ignoreCase = true) }) {
            return FilterDecision.Blocked(LocalizedText.keywordBlacklistHit())
        }
        if (ruleSet.keywordWhitelist.isNotEmpty() &&
            ruleSet.keywordWhitelist.none { keyword -> searchableText.contains(keyword, ignoreCase = true) }
        ) {
            return FilterDecision.Blocked(LocalizedText.keywordWhitelistMiss())
        }

        val dedupeWindowMillis = ruleSet.dedupeWindowSeconds * 1000L
        if (lastAcceptedAt != null && event.receivedAt - lastAcceptedAt < dedupeWindowMillis) {
            return FilterDecision.Blocked(LocalizedText.dedupeWindowHit())
        }
        return FilterDecision.Allowed
    }

    private fun detectChannelLoop(
        packageName: String,
        deliveryChannel: DeliveryChannel,
    ): String? = when (deliveryChannel) {
        DeliveryChannel.TELEGRAM ->
            packageName.takeIf { it in telegramPackages }?.let { LocalizedText.channelLoopPrevented("Telegram") }
        DeliveryChannel.SLACK ->
            packageName.takeIf { it in slackPackages }?.let { LocalizedText.channelLoopPrevented("Slack") }
        DeliveryChannel.BARK,
        DeliveryChannel.EMAIL,
            -> null
    }
}
