package com.project.topakuli.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tanaman_images",
    foreignKeys = [
        ForeignKey(
            entity = TanamanObatEntity::class,
            parentColumns = ["id"],
            childColumns = ["tanamanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tanamanId"])]
)
data class TanamanImageEntity(
    @PrimaryKey val id: String,
    val tanamanId: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)
