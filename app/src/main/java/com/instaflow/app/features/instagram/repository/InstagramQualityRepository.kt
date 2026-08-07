package com.instaflow.app.features.instagram.repository

import android.util.Log
import com.instaflow.app.features.instagram.models.InstagramFormat
import com.instaflow.app.features.instagram.models.InstagramMediaType
import com.instaflow.app.features.instagram.models.InstagramUiModel
import com.instaflow.app.util.Format
import com.instaflow.app.util.InstagramUrlType
import com.instaflow.app.util.VideoInfo
import java.util.Locale
import kotlin.math.max

private const val TAG = "InstagramQualityRepo"

object InstagramQualityRepository {

    // -------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------

    /**
     * Validates the raw [VideoInfo] returned by yt-dlp before mapping.
     * Throws [IllegalStateException] with a user-friendly message on failure.
     * Warnings are logged but do not throw — they represent recoverable missing fields.
     */
    fun validateVideoInfo(info: VideoInfo) {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Hard failures
        if (info.id.isBlank()) errors += "id is blank"
        if (info.webpageUrl.isNullOrBlank() && info.originalUrl.isNullOrBlank())
            errors += "webpageUrl and originalUrl both missing"
        if (info.extractorKey.isBlank()) errors += "extractorKey is blank (extractor did not fire)"

        // Soft warnings
        if (info.title.isNullOrBlank()) warnings += "title is blank (will use id as fallback)"
        if (info.uploader.isNullOrBlank() && info.channel.isNullOrBlank())
            warnings += "uploader and channel both missing"
        if (info.thumbnail.isNullOrBlank()) warnings += "thumbnail is missing"
        if (info.duration == null) warnings += "duration is null (expected for image posts)"
        val formatCount = info.formats?.size ?: 0
        if (formatCount == 0) warnings += "formats list is empty (expected for image posts)"

        if (warnings.isNotEmpty()) {
            Log.w(TAG, "[Validation] VideoInfo warnings for ${info.id}: ${warnings.joinToString("; ")}")
        }
        if (errors.isNotEmpty()) {
            val msg = "[Validation] VideoInfo hard errors for ${info.id}: ${errors.joinToString("; ")}"
            Log.e(TAG, msg)
            throw IllegalStateException(msg)
        }

        Log.d(TAG, "[Validation] VideoInfo OK — id=${info.id}, extractor=${info.extractorKey}, " +
                "duration=${info.duration}, formats=$formatCount, " +
                "uploader=${info.uploader ?: info.channel}, " +
                "webpageUrl=${info.webpageUrl}")
    }

    // -------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------

    /**
     * Maps a validated [VideoInfo] to [InstagramUiModel].
     *
     * @param hintType Optional URL type from [InstagramUrlValidator] so we can skip secondary
     *   URL-pattern detection. Null triggers the fallback heuristic (safe, but less accurate).
     */
    fun mapToUiModel(info: VideoInfo, hintType: InstagramUrlType? = null): InstagramUiModel {
        val durationSec = info.duration?.toInt() ?: 0
        val author = info.uploader?.ifEmpty { info.channel } ?: info.channel ?: "instagram_user"
        val captionText = info.title ?: ""
        val thumbUrl = info.thumbnail ?: ""

        val rawFormats = info.formats ?: emptyList()

        // Accurate detection of video vs photo
        val hasVideoCodec = rawFormats.any { it.vcodec != "none" && it.vcodec != null && !it.vcodec.contains("none") }
        val hasAudioCodec = rawFormats.any { it.acodec != "none" && it.acodec != null && !it.acodec.contains("none") }
        
        Log.d(TAG, "[Mapping] Mapping ${info.id}: duration=$durationSec, formats=${rawFormats.size}, hasVideo=$hasVideoCodec")

        // Use hint type from URL validator if provided; fall back to URL-pattern heuristic.
        val mediaType: InstagramMediaType = when (hintType) {
            InstagramUrlType.REEL        -> InstagramMediaType.REEL
            InstagramUrlType.HIGHLIGHT   -> InstagramMediaType.HIGHLIGHT
            InstagramUrlType.STORY       -> if (hasVideoCodec) InstagramMediaType.STORY else InstagramMediaType.IMAGE
            InstagramUrlType.PROFILE_PIC -> InstagramMediaType.PROFILE_PIC
            // POST and CAROUSEL: we still need to inspect yt-dlp output to know image vs video
            InstagramUrlType.POST,
            InstagramUrlType.CAROUSEL,
            InstagramUrlType.UNKNOWN,
            null -> {
                val isStory    = info.webpageUrl?.contains("/stories/") == true &&
                        !(info.webpageUrl?.contains("/highlights/") ?: false)
                val isHighlight = info.webpageUrl?.contains("/highlights/") == true
                val isReel     = info.webpageUrl?.contains("/reel/") == true
                val isProfile  = info.extractorKey.lowercase().contains("profile")
                when {
                    isProfile   -> InstagramMediaType.PROFILE_PIC
                    isReel      -> InstagramMediaType.REEL
                    isHighlight -> InstagramMediaType.HIGHLIGHT
                    isStory     -> if (hasVideoCodec) InstagramMediaType.STORY else InstagramMediaType.IMAGE
                    hasVideoCodec -> InstagramMediaType.VIDEO
                    else          -> InstagramMediaType.IMAGE
                }
            }
        }

        Log.d(TAG, "[Mapping] hintType=$hintType → mediaType=$mediaType, " +
                "formats=${rawFormats.size}, hasVideo=$hasVideoCodec, hasAudio=$hasAudioCodec, " +
                "duration=${info.duration}, extractor=${info.extractorKey}")
        
        // DEBUG: Log all formats for Instagram to diagnose "no sound" issues
        rawFormats.forEach { f ->
            Log.d(TAG, "[Pipeline] Format: id=${f.formatId}, ext=${f.ext}, vcodec=${f.vcodec}, acodec=${f.acodec}, abr=${f.abr}, note=${f.formatNote}")
        }

        val mappedFormats = mapRawFormats(rawFormats, durationSec, mediaType)
        val videoOpts = mappedFormats.filter { !it.isAudioOnly }
        val audioOpts = mappedFormats.filter { it.isAudioOnly }

        Log.i(TAG, "[QualityRepo] Mapped ${videoOpts.size} video options: " +
                videoOpts.joinToString { "${it.resolutionLabel} (formatId='${it.formatId}')" })
        if (audioOpts.isNotEmpty()) {
            Log.i(TAG, "[QualityRepo] Mapped ${audioOpts.size} audio options: " +
                    audioOpts.joinToString { "${it.resolutionLabel} (formatId='${it.formatId}')" })
        }

        return InstagramUiModel(
            mediaId = info.id ?: "",
            authorHandle = "@${author.removePrefix("@")}",
            caption = captionText,
            thumbnailUrl = thumbUrl,
            durationFormatted = if (mediaType == InstagramMediaType.IMAGE || mediaType == InstagramMediaType.PROFILE_PIC) "Photo" else formatDuration(durationSec),
            mediaTypeLabel = getMediaTypeLabel(mediaType, hasAudioCodec && !hasVideoCodec),
            videoQualityOptions = videoOpts,
            audioQualityOptions = audioOpts,
            hasAudioOption = hasAudioCodec
        )
    }

    private fun getMediaTypeLabel(type: InstagramMediaType, hasMusic: Boolean): String = when (type) {
        InstagramMediaType.IMAGE -> if (hasMusic) "Photo with Music" else "Image Post"
        InstagramMediaType.VIDEO -> "Video Post"
        InstagramMediaType.REEL -> "Instagram Reel"
        InstagramMediaType.CAROUSEL -> "Carousel Post"
        InstagramMediaType.STORY -> "Instagram Story"
        InstagramMediaType.HIGHLIGHT -> "Story Highlight"
        InstagramMediaType.PROFILE_PIC -> "Profile Picture"
    }

    private fun mapRawFormats(rawFormats: List<Format>, durationSeconds: Int, mediaType: InstagramMediaType): List<InstagramFormat> {
        val result = mutableListOf<InstagramFormat>()

        // 1. Photo Handling
        if (mediaType == InstagramMediaType.IMAGE || mediaType == InstagramMediaType.PROFILE_PIC) {
            val imageStreams = rawFormats.filter { (it.vcodec == "none" || it.vcodec == null) && (it.acodec == "none" || it.acodec == null) }
            
            if (imageStreams.isEmpty()) {
                // FALLBACK: yt-dlp returns no formats list for plain image posts;
                // the image URL is on the root VideoInfo.  Use empty formatId so
                // DownloadUtil.addOptionsForVideoDownloads() omits the "-f" flag and
                // lets yt-dlp auto-pick the image stream (avoids "no video in this post").
                result.add(InstagramFormat("", "Original HD Photo", 0, 0, "jpg", 0, false))
            } else {
                imageStreams.sortedByDescending { (it.height ?: 0.0) * (it.width ?: 0.0) }.forEachIndexed { index, f ->
                    val label = when (index) {
                        0 -> "Original HD"
                        1 -> "Standard Quality"
                        else -> "Thumbnail"
                    }
                    // Use f.formatId when available; empty string (not "best") as fallback.
                    result.add(InstagramFormat(f.formatId ?: "", label, f.width?.toInt() ?: 0, f.height?.toInt() ?: 0, "jpg", f.fileSize?.toLong() ?: f.fileSizeApprox?.toLong() ?: 0L, false))
                }
            }

            // Audio for Photos (Background Music)
            rawFormats.filter { it.acodec != "none" && it.acodec != null && (it.vcodec == "none" || it.vcodec == null) }.maxByOrNull { it.abr ?: 0.0 }?.let { f ->
                result.add(InstagramFormat(f.formatId ?: "bestaudio", "Background Music (M4A)", 0, 0, "m4a", calculateSizeBytes(f, durationSeconds), true))
            }
            
            return result
        }

        // 2. Video Handling
        val audioStreams = rawFormats.filter { it.acodec != "none" && it.acodec != null && (it.vcodec == "none" || it.vcodec == null) }
        
        // First Option: Optimal Auto (video + audio merged)
        result.add(InstagramFormat("bestvideo+bestaudio/best", "Optimal Quality (Auto)", 0, 0, "mp4", 0, false))

        val videoStreams = rawFormats.filter { it.vcodec != "none" && it.vcodec != null }.sortedByDescending { (it.height ?: 0.0) * (it.width ?: 0.0) }
        val seenRes = mutableSetOf<String>()

        for (f in videoStreams) {
            val h = f.height?.toInt() ?: 0
            val w = f.width?.toInt() ?: 0
            val maxDim = max(h, w)
            
            val bucket = when {
                maxDim >= 1080 -> 1080
                maxDim >= 720 -> 720
                maxDim >= 480 -> 480
                maxDim >= 360 -> 360
                else -> maxDim
            }
            
            val label = when (bucket) {
                1080 -> "High Definition (1080p)"
                720 -> "High Definition (720p)"
                480 -> "Standard (480p)"
                360 -> "Medium (360p)"
                else -> "${bucket}p Quality"
            }
            
            val rawFid = f.formatId ?: ""
            // Deduplicate by resolution bucket ($bucket) to show only ONE option per resolution
            val resKey = "$bucket"
            if (bucket > 0 && seenRes.add(resKey)) {
                // For Instagram, we ALWAYS try to merge with the best audio if we pick a specific video format.
                // This handles cases where DASH video is high res but silent.
                // We use a more inclusive audio selector to ensure sound is always captured.
                val formatSelector = when {
                    rawFid.isNotEmpty() -> "$rawFid+bestaudio/best"
                    else -> "bestvideo+bestaudio/best"
                }
                result.add(InstagramFormat(formatSelector, label, w, h, f.ext ?: "mp4", calculateSizeBytes(f, durationSeconds), false))
            }
        }

        // Video Only (Explicitly No Sound)
        if (videoStreams.isNotEmpty()) {
            videoStreams.maxByOrNull { (it.height ?: 0.0) * (it.width ?: 0.0) }?.let { f ->
                val rawFid = f.formatId ?: "bestvideo"
                result.add(InstagramFormat(rawFid, "Video Only (No Sound)", 0, 0, f.ext ?: "mp4", calculateSizeBytes(f, durationSeconds), false))
            }
        }

        // Only Music
        if (audioStreams.isNotEmpty()) {
            audioStreams.sortedByDescending { it.abr ?: 0.0 }.take(1).forEach { f ->
                result.add(InstagramFormat(f.formatId ?: "bestaudio", "Only Music (M4A/MP3)", 0, 0, "m4a", calculateSizeBytes(f, durationSeconds), true))
            }
        } else {
            result.add(InstagramFormat("bestaudio", "Only Music (Auto)", 0, 0, "m4a", 0, true))
        }

        return result
    }

    private fun calculateSizeBytes(format: Format, durationSeconds: Int): Long {
        val size = format.fileSize?.toLong() ?: format.fileSizeApprox?.toLong() ?: 0L
        if (size > 0L) return size
        val tbr = format.tbr ?: ((format.vbr ?: 0.0) + (format.abr ?: 0.0))
        if (tbr > 0.0 && durationSeconds > 0) return ((tbr * 1024 / 8) * durationSeconds).toLong()
        return 0L
    }

    private fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "00:00"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format(Locale.US, "%02d:%02d:%02d", h, m, s) else String.format(Locale.US, "%02d:%02d", m, s)
    }
}
