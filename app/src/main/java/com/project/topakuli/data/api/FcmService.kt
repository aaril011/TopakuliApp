package com.project.topakuli.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FcmService {
    @POST("v1/projects/{projectId}/messages:send")
    suspend fun sendNotification(
        @Path("projectId") projectId: String,
        @Header("Authorization") accessToken: String,
        @Body request: FcmRequest
    ): Response<ResponseBody>
}
