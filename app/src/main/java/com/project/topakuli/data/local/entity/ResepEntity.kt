package com.project.topakuli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resep")
data class ResepEntity(
    @PrimaryKey val id: String,
    val judul: String,
    val deskripsi: String,
    val bahan: String,
    val caraPembuatan: String,
    val kategori: String,
    val manfaat: String,
    val waktuPersiapan: String,
    val caraPakai: String,
    val frekuensi: String,
    val masaSimpan: String,
    val tingkatKesulitan: String,
    val tanamanId: String? = null, // Relasi ke TanamanObat
    val imageUrl: String? = null,
    val catatan: String? = null,
    var isSaved: Boolean = false
)
