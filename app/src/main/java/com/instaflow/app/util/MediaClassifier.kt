package com.instaflow.app.util

import android.util.Log

/**
 * Single source of truth for media classification across InstaFlow.
 * Eliminates divergence between history DB mapping and yt-dlp execution strategy.
 */
object MediaClassifier {

    private const val TAG = "MediaClassifier"

    enum class MediaType {
        IMAGE,
        VIDEO,
        REEL,
        CAROUSEL
    }

    /**
     * Classifies a [VideoInfo] object using strict metadata properties.
     */
    fun classify(videoInfo: VideoInfo, url: String = ""): MediaType {
        val isInstagram = url.contains("instagram") || url.contains("fbcdn.net") || videoInfo.extractorKey == "Instagram"
        val formats = videoInfo.formats.orEmpty()
        val requested = videoInfo.requestedDownloads.orEmpty()

        val hasVideoFormats = formats.any { it.vcodec != null && it.vcodec != "none" && !it.vcodec.contains("none") }
        val hasRequestedVideo = requested.any { it.vcodec != null && it.vcodec != "none" && !it.vcodec.contains("none") }
        val isVcodecNone = videoInfo.vcodec == null || videoInfo.vcodec == "none"
        val isImageExt = setOf("jpg", "jpeg", "png", "webp", "heic").contains(videoInfo.ext?.lowercase())
        val hasDuration = (videoInfo.duration ?: 0.0) > 0.0

        val type = when {
            // Explicit Reels always take priority
            isInstagram && (url.contains("/reel/") || url.contains("/reels/")) -> MediaType.REEL

            // If we have clear video signals, it's a VIDEO (or REEL if short)
            hasVideoFormats || hasRequestedVideo || hasDuration -> {
                if (isInstagram && videoInfo.duration != null && videoInfo.duration <= 90.0) MediaType.REEL
                else MediaType.VIDEO
            }

            // If it looks like an image, it's an IMAGE
            isImageExt && isVcodecNone -> MediaType.IMAGE

            // Fallback for Instagram: if no video signals and not a Reel URL, assume IMAGE for posts
            isInstagram && !url.contains("/reel/") -> MediaType.IMAGE

            // Default fallback
            else -> MediaType.VIDEO
        }

        Log.d(TAG, "[Classifier] ID=${videoInfo.id}, hasVideo=$hasVideoFormats, hasRequested=$hasRequestedVideo, vcodec=${videoInfo.vcodec}, ext=${videoInfo.ext}, duration=${videoInfo.duration} => Classified as $type")
        return type
    }

    fun isImageMedia(videoInfo: VideoInfo, url: String = ""): Boolean {
        return classify(videoInfo, url) == MediaType.IMAGE
    }
}
