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

    fun validateVideoInfo(info: VideoInfo) {
        if (info.id.isBlank()) throw IllegalStateException("id is blank")
        Log.d(TAG, "[Validation] VideoInfo OK - id=${info.id}")
    }

    fun mapToUiModel(info: VideoInfo, hintType: InstagramUrlType? = null): InstagramUiModel {
        val durationSec = info.duration?.toInt() ?: 0
        val author = info.uploader?.ifEmpty { info.channel } ?: info.channel ?: "instagram_user"
        val captionText = info.title ?: ""
        val thumbUrl = info.thumbnail ?: ""

        val rawFormats = info.formats ?: emptyList()

        val hasVideoCodec = rawFormats.any { it.vcodec != "none" && it.vcodec != null && !it.vcodec.contains("none") }
        val hasAudioCodec = rawFormats.any { it.acodec != "none" && it.acodec != null && !it.acodec.contains("none") }

        val mediaType: InstagramMediaType = when (hintType) {
            InstagramUrlType.REEL -> InstagramMediaType.REEL
            InstagramUrlType.HIGHLIGHT -> InstagramMediaType.HIGHLIGHT
            InstagramUrlType.STORY -> if (hasVideoCodec) InstagramMediaType.STORY else InstagramMediaType.IMAGE
            InstagramUrlType.PROFILE_PIC -> InstagramMediaType.PROFILE_PIC
            else -> {
                when {
                    hasVideoCodec -> InstagramMediaType.VIDEO
                    else -> InstagramMediaType.IMAGE
                }
            }
        }

        val mappedFormats = mapRawFormats(rawFormats, durationSec, mediaType)
        val videoOpts = mappedFormats.filter { !it.isAudioOnly }
        val audioOpts = mappedFormats.filter { it.isAudioOnly }

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

        if (mediaType == InstagramMediaType.IMAGE || mediaType == InstagramMediaType.PROFILE_PIC) {
            result.add(InstagramFormat("", "Original HD Photo", 0, 0, "jpg", 0, false))

            val audioFormat = rawFormats.filter { it.acodec != "none" && it.acodec != null && (it.vcodec == "none" || it.vcodec == null) }.maxByOrNull { it.abr ?: 0.0 }
            if (audioFormat != null) {
                result.add(0, InstagramFormat("merge_photo_audio", "Photo + Music (MP4 Video)", 1080, 1350, "mp4", 0, false, true))
                result.add(InstagramFormat(audioFormat.formatId ?: "bestaudio", "Only Music (M4A/MP3)", 0, 0, "m4a", 0, true))
            }
            return result
        }

        val audioStreams = rawFormats.filter { it.acodec != "none" && it.acodec != null && (it.vcodec == "none" || it.vcodec == null) }
        result.add(InstagramFormat("bestvideo+bestaudio/best", "Optimal Quality (Auto)", 0, 0, "mp4", 0, false))

        val videoStreams = rawFormats.filter { it.vcodec != "none" && it.vcodec != null }.sortedByDescending { (it.height ?: 0.0) * (it.width ?: 0.0) }
        val seenRes = mutableSetOf<String>()

        for (f in videoStreams) {
            val h = f.height?.toInt() ?: 0
            val w = f.width?.toInt() ?: 0
            val bucket = when {
                max(h, w) >= 1080 -> 1080
                max(h, w) >= 720 -> 720
                max(h, w) >= 480 -> 480
                else -> 360
            }
            if (seenRes.add("$bucket")) {
                val label = when (bucket) {
                    1080 -> "High Definition (1080p)"
                    720 -> "High Definition (720p)"
                    480 -> "Standard (480p)"
                    else -> "Medium (360p)"
                }
                result.add(InstagramFormat("${f.formatId}+bestaudio/best", label, w, h, f.ext ?: "mp4", calculateSizeBytes(f, durationSeconds), false))
            }
        }

        if (videoStreams.isNotEmpty()) {
            val bestVideo = videoStreams.first()
            result.add(InstagramFormat(bestVideo.formatId ?: "bestvideo", "Video Only (No Sound)", 0, 0, "mp4", 0, false))
        }

        if (audioStreams.isNotEmpty()) {
            val bestAudio = audioStreams.maxByOrNull { it.abr ?: 0.0 }
            result.add(InstagramFormat(bestAudio?.formatId ?: "bestaudio", "Only Music (M4A/MP3)", 0, 0, "m4a", 0, true))
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
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }
}
