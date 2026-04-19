package com.example.notifybridge.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.notifybridge.core.common.LocalizedText
import com.example.notifybridge.core.network.WebhookApi
import com.example.notifybridge.core.network.dto.BarkPushRequestDto
import com.example.notifybridge.domain.model.BarkGroupMode
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
        val serverUrl = settings.barkServerUrl.trim().trimEnd('/')
        val deviceKey = settings.barkDeviceKey.trim()
        if (serverUrl.isEmpty() || deviceKey.isEmpty()) {
            return ForwardResult.Failure(ForwardError.EndpointNotConfigured())
        }
        if (!isNetworkAvailable()) {
            return ForwardResult.Failure(ForwardError.NetworkUnavailable())
        }
        val bodyText = buildBarkBody(payload)
        return try {
            val response = webhookApi.postBarkPush(
                url = "$serverUrl/push",
                body = BarkPushRequestDto(
                    title = payload.title?.takeIf { it.isNotBlank() } ?: payload.appName,
                    subtitle = payload.appName,
                    body = if (settings.barkUseMarkdown) null else bodyText,
                    markdown = if (settings.barkUseMarkdown) bodyText else null,
                    deviceKey = deviceKey.takeIf { settings.barkDeviceKeys.isEmpty() },
                    deviceKeys = settings.barkDeviceKeys.takeIf { it.isNotEmpty() },
                    group = resolveBarkGroup(settings, payload),
                    url = settings.barkUrl.takeIf { it.isNotBlank() },
                    level = settings.barkLevel,
                    volume = settings.barkVolume,
                    badge = settings.barkBadge,
                    call = settings.barkCall.toBarkFlag(),
                    autoCopy = settings.barkAutoCopy.toBarkFlag(),
                    copy = settings.barkCopy.takeIf { it.isNotBlank() },
                    sound = settings.barkSound.takeIf { it.isNotBlank() },
                    icon = settings.barkIcon.takeIf { it.isNotBlank() },
                    image = settings.barkImage.takeIf { it.isNotBlank() },
                    ciphertext = settings.barkCiphertext.takeIf { it.isNotBlank() },
                    isArchive = settings.barkIsArchive?.toBarkFlag(),
                    action = settings.barkAction.takeIf { it.isNotBlank() },
                    id = settings.barkNotificationId.takeIf { it.isNotBlank() },
                    delete = settings.barkDelete.toBarkFlag(),
                ),
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
            ForwardResult.Failure(ForwardError.Timeout(e.message ?: LocalizedText.timeout()))
        } catch (e: SerializationException) {
            ForwardResult.Failure(ForwardError.SerializationError(e.message ?: LocalizedText.serializationFailed()))
        } catch (e: HttpException) {
            ForwardResult.Failure(ForwardError.HttpError(e.code(), e.message()))
        } catch (e: IOException) {
            ForwardResult.Failure(ForwardError.ConnectionFailure(e.message ?: LocalizedText.connectionFailed()))
        } catch (e: Exception) {
            ForwardResult.Failure(ForwardError.Unknown(e.message ?: LocalizedText.unknownError()))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun buildBarkBody(payload: ForwardPayload): String {
        val content = listOfNotNull(
            payload.text?.takeIf { it.isNotBlank() },
            payload.subText?.takeIf { it.isNotBlank() },
        ).joinToString("\n")

        val fallbackContent = if (content.isBlank()) LocalizedText.noBody() else content
        return buildString {
            appendLine(fallbackContent)
            appendLine()
            appendLine("App: ${payload.appName}")
            appendLine("Package: ${payload.appPackage}")
            appendLine("Device: ${payload.deviceModel} / Android ${payload.androidVersion}")
        }.trim()
    }

    private fun resolveBarkGroup(
        settings: com.example.notifybridge.domain.model.AppSettings,
        payload: ForwardPayload,
    ): String = when (settings.barkGroupMode) {
        BarkGroupMode.APP_NAME -> payload.appName
        BarkGroupMode.DEVICE_NAME -> payload.deviceModel
        BarkGroupMode.APP_NAME_AT_DEVICE_NAME -> "${payload.appName}@${payload.deviceModel}"
        BarkGroupMode.CUSTOM -> settings.barkGroupCustom.takeIf { it.isNotBlank() } ?: payload.appPackage
    }

    private fun Boolean.toBarkFlag(): String? = if (this) "1" else null
}
