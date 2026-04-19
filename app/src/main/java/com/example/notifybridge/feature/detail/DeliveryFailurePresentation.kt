package com.example.notifybridge.feature.detail

import com.example.notifybridge.core.common.LocalizedText
import com.example.notifybridge.domain.model.DeliveryAttempt
import com.example.notifybridge.domain.model.DeliveryRecord
import com.example.notifybridge.domain.model.DeliveryStatus

data class DeliveryFailurePresentation(
    val category: String,
    val summary: String,
    val suggestion: String,
    val retryable: Boolean,
)

fun DeliveryRecord.toFailurePresentation(): DeliveryFailurePresentation? {
    if (status == DeliveryStatus.SUCCESS) return null
    return classifyFailure(responseCode = responseCode, errorMessage = errorMessage)
}

fun DeliveryAttempt.toFailurePresentation(): DeliveryFailurePresentation? {
    return classifyFailure(responseCode = responseCode, errorMessage = errorMessage)
}

private fun classifyFailure(
    responseCode: Int?,
    errorMessage: String?,
): DeliveryFailurePresentation? {
    if (responseCode == null && errorMessage.isNullOrBlank()) return null

    val normalizedMessage = errorMessage.orEmpty().lowercase()
    return when {
        responseCode == 401 || responseCode == 403 -> DeliveryFailurePresentation(
            category = LocalizedText.authFailureCategory(),
            summary = LocalizedText.authFailureSummary(),
            suggestion = LocalizedText.authFailureSuggestion(),
            retryable = false,
        )
        responseCode == 404 -> DeliveryFailurePresentation(
            category = LocalizedText.notFoundCategory(),
            summary = LocalizedText.notFoundSummary(),
            suggestion = LocalizedText.notFoundSuggestion(),
            retryable = false,
        )
        responseCode == 408 || normalizedMessage.contains("超时") -> DeliveryFailurePresentation(
            category = LocalizedText.timeoutCategory(),
            summary = LocalizedText.timeoutSummary(),
            suggestion = LocalizedText.timeoutSuggestion(),
            retryable = true,
        )
        responseCode == 429 -> DeliveryFailurePresentation(
            category = LocalizedText.rateLimitCategory(),
            summary = LocalizedText.rateLimitSummary(),
            suggestion = LocalizedText.rateLimitSuggestion(),
            retryable = true,
        )
        responseCode != null && responseCode >= 500 -> DeliveryFailurePresentation(
            category = LocalizedText.serverErrorCategory(),
            summary = LocalizedText.serverErrorSummary(),
            suggestion = LocalizedText.serverErrorSuggestion(),
            retryable = true,
        )
        responseCode != null && responseCode in 400..499 -> DeliveryFailurePresentation(
            category = LocalizedText.clientErrorCategory(),
            summary = LocalizedText.clientErrorSummary(),
            suggestion = LocalizedText.clientErrorSuggestion(),
            retryable = false,
        )
        normalizedMessage.contains("未配置 webhook") -> DeliveryFailurePresentation(
            category = LocalizedText.endpointCategory(),
            summary = LocalizedText.endpointSummary(),
            suggestion = LocalizedText.endpointSuggestion(),
            retryable = false,
        )
        normalizedMessage.contains("网络不可用") -> DeliveryFailurePresentation(
            category = LocalizedText.networkCategory(),
            summary = LocalizedText.networkSummary(),
            suggestion = LocalizedText.networkSuggestion(),
            retryable = true,
        )
        normalizedMessage.contains("unable to resolve host") ||
            normalizedMessage.contains("dns") ||
            normalizedMessage.contains("failed to connect") ||
            normalizedMessage.contains("连接失败") -> DeliveryFailurePresentation(
            category = LocalizedText.dnsCategory(),
            summary = LocalizedText.dnsSummary(),
            suggestion = LocalizedText.dnsSuggestion(),
            retryable = true,
        )
        normalizedMessage.contains("序列化") -> DeliveryFailurePresentation(
            category = LocalizedText.serializationCategory(),
            summary = LocalizedText.serializationSummary(),
            suggestion = LocalizedText.serializationSuggestion(),
            retryable = false,
        )
        else -> DeliveryFailurePresentation(
            category = LocalizedText.unknownCategory(),
            summary = LocalizedText.unknownSummary(errorMessage),
            suggestion = LocalizedText.unknownSuggestion(),
            retryable = true,
        )
    }
}
