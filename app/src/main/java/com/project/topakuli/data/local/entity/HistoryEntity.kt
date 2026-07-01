package com.project.topakuli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identifikasi_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tanamanId: String?,
    val namaTanaman: String,
    val namaLatin: String = "",
    val namaDaerah: String = "",
    val akurasi: Double,
    val imagePath: String, // Lokasi path gambar di storage lokal HP
    val timestamp: Long = System.currentTimeMillis()
)
