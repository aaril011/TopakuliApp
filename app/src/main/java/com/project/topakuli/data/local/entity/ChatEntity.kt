package com.project.topakuli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "system", "user", "options", "contribution_menu", "input_name", "input_plant", "input_recipe", "tips", "suggestion", "contact_info", "correction_search"
    val message: String? = null,
    val inputLabel: String? = null,
    val inputHint: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
