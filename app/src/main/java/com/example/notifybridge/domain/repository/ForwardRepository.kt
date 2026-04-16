package com.example.notifybridge.domain.repository

import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.ForwardPayload
import com.example.notifybridge.domain.model.ForwardResult

interface ForwardRepository {
    suspend fun sendPayload(payload: ForwardPayload): ForwardResult
    suspend fun sendTestPayload(settings: AppSettings? = null): ForwardResult
}
