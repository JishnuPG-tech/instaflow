package com.instaflow.app.util

import android.util.Log
import com.instaflow.app.features.instagram.models.InstagramUiModel
import com.instaflow.app.features.instagram.repository.InstagramQualityRepository

private const val TAG = "InstagramHandlerDispatch"

/**
 * Phase 3 — Dispatch table.
 *
 * Routes a [VideoInfo] to [InstagramQualityRepository.mapToUiModel] with the correct
 * [InstagramUrlType] hint so the repository never needs to re-parse the URL.
 *
 * The old handler files (InstagramImagePostHandler, InstagramVideoPostHandler, etc.) parsed
 * from raw yt-dlp JSON strings — an approach that predates the current [VideoInfo] integration.
 * They are marked @Deprecated and kept for reference until the new pipeline has been
 * proven stable across all six content types on a real device.
 */
object InstagramHandlerDispatch {

    /**
     * Maps a [VideoInfo] to [InstagramUiModel] using the pre-classified [urlType].
     *
     * @param info    The [VideoInfo] returned by yt-dlp, already validated via
     *                [InstagramQualityRepository.validateVideoInfo].
     * @param urlType The [InstagramUrlType] resolved by [InstagramUrlValidator] before the
     *                yt-dlp call. Callers should pass the type from [InstagramUrlValidator.parseUrl].
     */
    fun handle(info: VideoInfo, urlType: InstagramUrlType): InstagramUiModel {
        Log.d(TAG, "[Dispatch] urlType=$urlType -> calling InstagramQualityRepository.mapToUiModel")
        return InstagramQualityRepository.mapToUiModel(info, hintType = urlType)
    }
}
