package com.project.topakuli.data.local.entity

data class ActivityLog(
    val id: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val type: String = "", // "Tambah", "Ubah", "Hapus"
    val target: String = "", // "Tanaman", "Resep"
    val targetName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
