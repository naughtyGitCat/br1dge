package com.example.notifybridge.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.notifybridge.core.common.LocalizedText
import com.example.notifybridge.core.network.WebhookApi
import com.example.notifybridge.core.network.dto.BarkPushRequestDto
import com.example.notifybridge.core.network.dto.SlackWebhookRequestDto
import com.example.notifybridge.core.network.dto.TelegramSendMessageDto
import com.example.notifybridge.domain.model.AppSettings
import com.example.notifybridge.domain.model.BarkGroupMode
import com.example.notifybridge.domain.model.DeliveryChannel
import com.example.notifybridge.domain.model.EmailSecurityMode
import com.example.notifybridge.domain.model.ForwardError
import com.example.notifybridge.domain.model.ForwardPayload
import com.example.notifybridge.domain.model.ForwardResult
import com.example.notifybridge.domain.repository.ForwardRepository
import com.example.notifybridge.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Properties
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

    override suspend fun sendTestPayload(settings: AppSettings?): ForwardResult {
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
                deviceModel = Build.MODEL ?: "Unknown",
                androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            ),
            settings = resolvedSettings,
        )
    }

    private suspend fun sendPayload(
        payload: ForwardPayload,
        settings: AppSettings,
    ): ForwardResult {
        if (!isNetworkAvailable()) {
            return ForwardResult.Failure(ForwardError.NetworkUnavailable())
        }
        return when (settings.deliveryChannel) {
            DeliveryChannel.BARK -> sendBark(payload, settings)
            DeliveryChannel.TELEGRAM -> sendTelegram(payload, settings)
            DeliveryChannel.SLACK -> sendSlack(payload, settings)
            DeliveryChannel.EMAIL -> sendEmail(payload, settings)
        }
    }

    private suspend fun sendBark(
        payload: ForwardPayload,
        settings: AppSettings,
    ): ForwardResult {
        val serverUrl = settings.barkServerUrl.trim().trimEnd('/')
        val deviceKey = settings.barkDeviceKey.trim()
        if (serverUrl.isEmpty() || (deviceKey.isEmpty() && settings.barkDeviceKeys.isEmpty())) {
            return ForwardResult.Failure(ForwardError.EndpointNotConfigured())
        }
        val bodyText = buildPlainBody(payload)
        return executeHttpRequest {
            webhookApi.postBarkPush(
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
                    call = settings.barkCall.toFlag(),
                    autoCopy = settings.barkAutoCopy.toFlag(),
                    copy = settings.barkCopy.takeIf { it.isNotBlank() },
                    sound = settings.barkSound.takeIf { it.isNotBlank() },
                    icon = settings.barkIcon.takeIf { it.isNotBlank() },
                    image = settings.barkImage.takeIf { it.isNotBlank() },
                    ciphertext = settings.barkCiphertext.takeIf { it.isNotBlank() },
                    isArchive = settings.barkIsArchive?.toFlag(),
                    action = settings.barkAction.takeIf { it.isNotBlank() },
                    id = settings.barkNotificationId.takeIf { it.isNotBlank() },
                    delete = settings.barkDelete.toFlag(),
                ),
            )
        }
    }

    private suspend fun sendTelegram(
        payload: ForwardPayload,
        settings: AppSettings,
    ): ForwardResult {
        val token = settings.telegramBotToken.trim()
        val chatId = settings.telegramChatId.trim()
        if (token.isEmpty() || chatId.isEmpty()) {
            return ForwardResult.Failure(ForwardError.EndpointNotConfigured())
        }
        return executeHttpRequest {
            webhookApi.postTelegramMessage(
                url = "https://api.telegram.org/bot$token/sendMessage",
                body = TelegramSendMessageDto(
                    chatId = chatId,
                    text = buildTelegramBody(payload),
                    messageThreadId = settings.telegramMessageThreadId.trim().toIntOrNull(),
                    disableNotification = settings.telegramDisableNotification,
                    parseMode = settings.telegramUseMarkdown.thenTake("MarkdownV2"),
                ),
            )
        }
    }

    private suspend fun sendSlack(
        payload: ForwardPayload,
        settings: AppSettings,
    ): ForwardResult {
        val webhookUrl = settings.slackWebhookUrl.trim()
        if (webhookUrl.isEmpty()) {
            return ForwardResult.Failure(ForwardError.EndpointNotConfigured())
        }
        return executeHttpRequest {
            webhookApi.postSlackWebhook(
                url = webhookUrl,
                body = SlackWebhookRequestDto(
                    text = buildSlackBody(payload),
                    username = settings.slackUsername.takeIf { it.isNotBlank() },
                    iconEmoji = settings.slackIconEmoji.takeIf { it.isNotBlank() },
                ),
            )
        }
    }

    private suspend fun sendEmail(
        payload: ForwardPayload,
        settings: AppSettings,
    ): ForwardResult = withContext(Dispatchers.IO) {
        val host = settings.emailSmtpHost.trim()
        val username = settings.emailUsername.trim()
        val password = settings.emailPassword
        val from = settings.emailFromAddress.trim()
        val to = settings.emailToAddress.trim()
        if (host.isEmpty() || username.isEmpty() || password.isEmpty() || from.isEmpty() || to.isEmpty()) {
            return@withContext ForwardResult.Failure(ForwardError.EndpointNotConfigured())
        }
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.host", host)
                put("mail.smtp.port", settings.emailSmtpPort.toString())
                when (settings.emailSecurityMode) {
                    EmailSecurityMode.NONE -> {
                        put("mail.smtp.starttls.enable", "false")
                        put("mail.smtp.ssl.enable", "false")
                    }
                    EmailSecurityMode.STARTTLS -> {
                        put("mail.smtp.starttls.enable", "true")
                        put("mail.smtp.ssl.enable", "false")
                    }
                    EmailSecurityMode.SSL_TLS -> {
                        put("mail.smtp.starttls.enable", "false")
                        put("mail.smtp.ssl.enable", "true")
                    }
                }
                put("mail.smtp.connectiontimeout", "${settings.connectTimeoutSeconds * 1000}")
                put("mail.smtp.timeout", "${settings.readTimeoutSeconds * 1000}")
            }
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(from))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                subject = buildEmailSubject(payload, settings)
                setText(buildPlainBody(payload), Charsets.UTF_8.name())
            }
            Transport.send(message)
            ForwardResult.Success
        } catch (e: SocketTimeoutException) {
            ForwardResult.Failure(ForwardError.Timeout(e.message ?: LocalizedText.timeout()))
        } catch (e: IOException) {
            ForwardResult.Failure(ForwardError.ConnectionFailure(e.message ?: LocalizedText.connectionFailed()))
        } catch (e: Exception) {
            ForwardResult.Failure(ForwardError.Unknown(e.message ?: LocalizedText.unknownError()))
        }
    }

    private suspend fun executeHttpRequest(
        block: suspend () -> retrofit2.Response<okhttp3.ResponseBody>,
    ): ForwardResult {
        return try {
            val response = block()
            if (response.isSuccessful) {
                ForwardResult.Success
            } else {
                ForwardResult.Failure(
                    ForwardError.HttpError(
                        code = response.code(),
                        message = "HTTP ${response.code()} ${response.message()}",
                    ),
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

    private fun buildPlainBody(payload: ForwardPayload): String {
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

    private fun buildTelegramBody(payload: ForwardPayload): String {
        return buildString {
            appendLine("*${escapeTelegramMarkdown(payload.title?.takeIf { it.isNotBlank() } ?: payload.appName)}*")
            appendLine()
            appendLine(escapeTelegramMarkdown(buildPlainBody(payload)))
        }.trim()
    }

    private fun buildSlackBody(payload: ForwardPayload): String {
        return buildString {
            append("*${payload.title?.takeIf { it.isNotBlank() } ?: payload.appName}*")
            append("\n")
            append(buildPlainBody(payload))
        }
    }

    private fun buildEmailSubject(payload: ForwardPayload, settings: AppSettings): String {
        val title = payload.title?.takeIf { it.isNotBlank() } ?: payload.appName
        val prefix = settings.emailSubjectPrefix.trim()
        return listOfNotNull(prefix.takeIf { it.isNotEmpty() }, title).joinToString(" ")
    }

    private fun resolveBarkGroup(settings: AppSettings, payload: ForwardPayload): String = when (settings.barkGroupMode) {
        BarkGroupMode.APP_NAME -> payload.appName
        BarkGroupMode.DEVICE_NAME -> payload.deviceModel
        BarkGroupMode.APP_NAME_AT_DEVICE_NAME -> "${payload.appName}@${payload.deviceModel}"
        BarkGroupMode.CUSTOM -> settings.barkGroupCustom.takeIf { it.isNotBlank() } ?: payload.appPackage
    }

    private fun Boolean.toFlag(): String? = if (this) "1" else null

    private fun Boolean.thenTake(value: String): String? = if (this) value else null

    private fun escapeTelegramMarkdown(value: String): String {
        val reserved = "_*[]()~`>#+-=|{}.!"
        return buildString {
            value.forEach { char ->
                if (char in reserved) append('\\')
                append(char)
            }
        }
    }
}
