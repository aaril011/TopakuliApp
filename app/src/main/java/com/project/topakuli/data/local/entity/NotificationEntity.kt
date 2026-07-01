package com.project.topakuli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val type: String, // "update", "added", "recipe"
    val targetId: String? = null, // ID Tanaman atau Resep
    val timestamp: Long,
    var isRead: Boolean = false,
    val isDeletedLocally: Boolean = false
)
