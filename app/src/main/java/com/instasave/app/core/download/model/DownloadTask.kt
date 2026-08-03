package com.instasave.app.core.download.model

import java.util.UUID

enum class DownloadState {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DownloadTask(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val mimeType: String,
    val isVideo: Boolean,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val state: DownloadState = DownloadState.QUEUED,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes.toFloat() else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt()
}
