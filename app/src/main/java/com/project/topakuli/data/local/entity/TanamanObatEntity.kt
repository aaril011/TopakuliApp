package com.project.topakuli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tanaman_obat")
data class TanamanObatEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val namaDaerah: String,
    val namaLatin: String,
    val deskripsi: String,
    val khasiat: String,
    val imageUrl: String? = null,
    var isFavorit: Boolean = false
)
