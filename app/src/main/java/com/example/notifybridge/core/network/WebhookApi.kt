package com.example.notifybridge.core.network

import com.example.notifybridge.core.network.dto.BarkPushRequestDto
import com.example.notifybridge.core.network.dto.SlackWebhookRequestDto
import com.example.notifybridge.core.network.dto.TelegramSendMessageDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface WebhookApi {
    @POST
    suspend fun postBarkPush(
        @Url url: String,
        @Body body: BarkPushRequestDto,
    ): Response<ResponseBody>

    @POST
    suspend fun postTelegramMessage(
        @Url url: String,
        @Body body: TelegramSendMessageDto,
    ): Response<ResponseBody>

    @POST
    suspend fun postSlackWebhook(
        @Url url: String,
        @Body body: SlackWebhookRequestDto,
    ): Response<ResponseBody>
}
