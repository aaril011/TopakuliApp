package com.project.topakuli.data.local.entity

data class UserContribution(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val displayId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isReadByAdmin: Boolean = false,
    val status: String = "active" // "active", "resolved", "pending"
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val senderType: String = "" // "user" or "admin"
)
