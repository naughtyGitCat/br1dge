package com.example.notifybridge.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.notifybridge.core.network.WebhookApi
import com.example.notifybridge.core.network.dto.WebhookRequestDto
import com.example.notifybridge.domain.model.ForwardError
import com.example.notifybridge.domain.model.ForwardPayload
import com.example.notifybridge.domain.model.ForwardResult
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForwardRepositoryImpl @Inject constructor(
    private val webhookApi: WebhookApi,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ForwardRepository {

    override suspend fun sendPayload(payload: ForwardPayload): ForwardResult {
        val settings = settingsRepository.getSettings()
        return sendPayload(payload = payload, settings = settings)
    }

    override suspend fun sendTestPayload(settings: com.example.notifybridge.domain.model.AppSettings?): ForwardResult {
        val resolvedSettings = settings ?: settingsRepository.getSettings()
        return sendPayload(
            payload = ForwardPayload(
                appPackage = context.packageName,
                appName = "NotifyBridge",
                title = "NotifyBridge Test",
                text = "This is a test payload.",
                subText = "MVP",
                postTime = System.currentTimeMillis(),
                receivedAt = System.currentTimeMillis(),
                deviceModel = android.os.Build.MODEL ?: "Unknown",
                androidVersion = android.os.Build.VERSION.RELEASE ?: "Unknown",
            ),
            settings = resolvedSettings,
        )
    }

    private suspend fun sendPayload(
        payload: ForwardPayload,
        settings: com.example.notifybridge.domain.model.AppSettings,
    ): ForwardResult {
        val endpoint = settings.webhookUrl.trim()
        if (endpoint.isEmpty()) {
            return ForwardResult.Failure(ForwardError.EndpointNotConfigured)
        }
        if (!isNetworkAvailable()) {
            return ForwardResult.Failure(ForwardError.NetworkUnavailable)
        }
        val token = settings.bearerToken.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
        return try {
            val response = webhookApi.postWebhook(
                url = endpoint,
                body = WebhookRequestDto(
                    appPackage = payload.appPackage,
                    appName = payload.appName,
                    title = payload.title,
                    text = payload.text,
                    subText = payload.subText,
                    postTime = payload.postTime,
                    receivedAt = payload.receivedAt,
                    deviceModel = payload.deviceModel,
                    androidVersion = payload.androidVersion,
                ),
                authorization = token,
            )
            if (response.isSuccessful) {
                ForwardResult.Success
            } else {
                ForwardResult.Failure(
                    ForwardError.HttpError(
                        code = response.code(),
                        message = "HTTP ${response.code()} ${response.message()}",
                    )
                )
            }
        } catch (e: SocketTimeoutException) {
            ForwardResult.Failure(ForwardError.Timeout(e.message ?: "请求超时"))
        } catch (e: SerializationException) {
            ForwardResult.Failure(ForwardError.SerializationError(e.message ?: "序列化失败"))
        } catch (e: HttpException) {
            ForwardResult.Failure(ForwardError.HttpError(e.code(), e.message()))
        } catch (e: IOException) {
            ForwardResult.Failure(ForwardError.ConnectionFailure(e.message ?: "连接失败"))
        } catch (e: Exception) {
            ForwardResult.Failure(ForwardError.Unknown(e.message ?: "未知错误"))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
