package com.instasave.app.core.extractor

import com.instasave.app.core.network.generated.model.MediaInfo

/**
 * Interface contract for on-device fallback metadata extraction.
 * Serves as the local fallback when the remote FastAPI backend is unreachable.
 */
interface OnDeviceExtractor {
    /**
     * Attempts to resolve media info directly on device.
     * @param url Instagram media URL
     * @return Result containing MediaInfo schema or Throwable exception
     */
    suspend fun extract(url: String): Result<MediaInfo>
}

/**
 * Stub implementation of OnDeviceExtractor for Phase 1.
 */
class DefaultOnDeviceExtractor : OnDeviceExtractor {
    override suspend fun extract(url: String): Result<MediaInfo> {
        return Result.failure(
            UnsupportedOperationException("On-device yt-dlp native binary fallback queued for Phase 2 integration.")
        )
    }
}
