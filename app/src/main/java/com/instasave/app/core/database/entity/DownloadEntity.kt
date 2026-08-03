package com.instasave.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_history")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val author: String,
    val mediaType: String, // "video", "photo", "carousel"
    val formatLabel: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
)
