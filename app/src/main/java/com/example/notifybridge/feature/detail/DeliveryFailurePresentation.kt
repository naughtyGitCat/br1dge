package com.example.notifybridge.feature.detail

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
            category = "鉴权失败",
            summary = "服务端拒绝了当前认证信息。",
            suggestion = "检查 Bearer Token 是否过期，或确认服务端是否要求额外 Header。",
            retryable = false,
        )
        responseCode == 404 -> DeliveryFailurePresentation(
            category = "地址不存在",
            summary = "Webhook 地址返回 404，服务端路由可能不存在。",
            suggestion = "检查 Settings 里的 Webhook URL 是否正确，尤其是路径和末尾斜杠。",
            retryable = false,
        )
        responseCode == 408 || normalizedMessage.contains("超时") -> DeliveryFailurePresentation(
            category = "请求超时",
            summary = "请求没有在预期时间内完成。",
            suggestion = "先确认网络质量，再检查服务端是否处理过慢；这类错误通常适合重试。",
            retryable = true,
        )
        responseCode == 429 -> DeliveryFailurePresentation(
            category = "被服务端限流",
            summary = "服务端认为请求过于频繁，暂时拒绝处理。",
            suggestion = "稍后再试，并考虑在服务端或客户端增加节流策略。",
            retryable = true,
        )
        responseCode != null && responseCode >= 500 -> DeliveryFailurePresentation(
            category = "服务端异常",
            summary = "Webhook 服务端返回了 5xx 错误。",
            suggestion = "优先检查服务端日志；客户端可以继续自动重试。",
            retryable = true,
        )
        responseCode != null && responseCode in 400..499 -> DeliveryFailurePresentation(
            category = "请求参数异常",
            summary = "服务端返回 4xx，说明当前请求不符合接口要求。",
            suggestion = "检查服务端对 JSON 字段、Header 和认证方式的要求。",
            retryable = false,
        )
        normalizedMessage.contains("未配置 webhook") -> DeliveryFailurePresentation(
            category = "未配置端点",
            summary = "当前没有可用的 Webhook 地址。",
            suggestion = "先在 Settings 里填写 Webhook URL，再重新测试发送。",
            retryable = false,
        )
        normalizedMessage.contains("网络不可用") -> DeliveryFailurePresentation(
            category = "网络不可用",
            summary = "设备当前没有可用网络，发送无法开始。",
            suggestion = "确认设备联网状态，必要时关闭省电限制后再试。",
            retryable = true,
        )
        normalizedMessage.contains("unable to resolve host") ||
            normalizedMessage.contains("dns") ||
            normalizedMessage.contains("failed to connect") ||
            normalizedMessage.contains("连接失败") -> DeliveryFailurePresentation(
            category = "连接或 DNS 失败",
            summary = "请求没有成功建立到目标服务端的连接。",
            suggestion = "检查域名解析、证书、端口和代理设置；若地址是内网，也要确认设备可达。",
            retryable = true,
        )
        normalizedMessage.contains("序列化") -> DeliveryFailurePresentation(
            category = "序列化失败",
            summary = "客户端在构造或解析请求数据时发生异常。",
            suggestion = "优先检查本地 payload 结构和服务端字段约定，这类错误通常不适合盲目重试。",
            retryable = false,
        )
        else -> DeliveryFailurePresentation(
            category = "未知异常",
            summary = errorMessage ?: "发送失败，但没有更多上下文。",
            suggestion = "先查看本页的请求 payload 和尝试历史，再结合服务端日志排查。",
            retryable = true,
        )
    }
}
