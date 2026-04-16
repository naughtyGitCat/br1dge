package com.example.notifybridge.core.network

import com.example.notifybridge.core.network.dto.WebhookRequestDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface WebhookApi {
    @POST
    suspend fun postWebhook(
        @Url url: String,
        @Body body: WebhookRequestDto,
        @Header("Authorization") authorization: String?,
    ): Response<ResponseBody>
}
