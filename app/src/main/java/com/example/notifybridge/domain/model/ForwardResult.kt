package com.example.notifybridge.domain.model

sealed class ForwardResult {
    data object Success : ForwardResult()
    data class Failure(val error: ForwardError) : ForwardResult()
}

sealed class ForwardError(
    open val message: String,
    open val retryable: Boolean,
) {
    data object EndpointNotConfigured : ForwardError("未配置 Webhook 地址", false)
    data object NetworkUnavailable : ForwardError("网络不可用", true)
    data class ConnectionFailure(override val message: String) : ForwardError(message, true)
    data class Timeout(override val message: String) : ForwardError(message, true)
    data class HttpError(val code: Int, override val message: String) : ForwardError(
        message = message,
        retryable = code == 408 || code == 429 || code >= 500
    )
    data class SerializationError(override val message: String) : ForwardError(message, false)
    data class Unknown(override val message: String) : ForwardError(message, true)
}
