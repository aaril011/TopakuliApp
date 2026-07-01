package com.project.topakuli.data.api

data class FcmRequest(
    val message: FcmMessage
)

data class FcmMessage(
    val topic: String? = null,
    val token: String? = null,
    val notification: FcmNotification? = null,
    val data: Map<String, String>? = null
)

data class FcmNotification(
    val title: String,
    val body: String
)
