package com.junkfood.seal.features.instagram.repository

import com.junkfood.seal.features.instagram.models.InstagramFormat
import com.junkfood.seal.features.instagram.models.InstagramMediaModel
import com.junkfood.seal.features.instagram.models.InstagramMediaType
import com.junkfood.seal.features.instagram.models.InstagramUiModel
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.VideoInfo
import java.util.Locale

object InstagramQualityRepository {

    /**
     * Maps raw yt-dlp VideoInfo into a clean, decoupled InstagramUiModel.
     * Prevents raw format IDs (dash-1230...), codec strings, and "Unknown" cards from ever reaching the UI.
     */
    fun mapToUiModel(info: VideoInfo): InstagramUiModel {
        val durationSec = info.duration?.toInt() ?: 0
        val author = info.uploader?.ifEmpty { info.channel } ?: "instagram_user"
        val captionText = info.title ?: ""
        val thumbUrl = info.thumbnail ?: info.thumbnails?.firstOrNull()?.url ?: ""

        val rawFormats = info.formats ?: emptyList()
        val mappedFormats = mapRawFormats(rawFormats, durationSec)

        val videoTracks = mappedFormats.filter { !it.isAudioOnly }
        val audioTracks = mappedFormats.filter { it.isAudioOnly }

        val isVideoContent = durationSec > 0 || videoTracks.isNotEmpty()

        val domainModel = InstagramMediaModel(
            id = info.id ?: "",
            shortcode = info.id ?: "",
            type = if (isVideoContent) InstagramMediaType.REEL else InstagramMediaType.IMAGE,
            ownerUsername = author,
            caption = captionText,
            thumbnailUrl = thumbUrl,
            durationSeconds = durationSec,
            metadataLoaded = true,
            availableVideoTracks = videoTracks,
            availableAudioTracks = audioTracks
        )

        return InstagramUiModel(
            mediaId = domainModel.id,
            authorHandle = "@${author.removePrefix("@")}",
            caption = captionText,
            thumbnailUrl = thumbUrl,
            durationFormatted = formatDuration(durationSec),
            mediaTypeLabel = if (isVideoContent) "Reel / Video" else "Photo",
            videoQualityOptions = videoTracks,
            audioQualityOptions = audioTracks,
            hasAudioOption = audioTracks.isNotEmpty() || isVideoContent
        )
    }

    private fun mapRawFormats(rawFormats: List<Format>, durationSeconds: Int): List<InstagramFormat> {
        if (rawFormats.isEmpty()) return emptyList()

        val result = mutableListOf<InstagramFormat>()

        // 1. Separate Video and Audio streams
        val videoStreams = rawFormats.filter { it.vcodec != "none" && it.vcodec != null }
        val audioStreams = rawFormats.filter { it.acodec != "none" && it.acodec != null && (it.vcodec == "none" || it.vcodec == null) }

        // Map best video resolutions
        val sortedVideo = videoStreams.sortedByDescending { (it.height ?: 0) * (it.width ?: 0) }

        val seenHeights = mutableSetOf<Int>()
        for (format in sortedVideo) {
            val h = format.height ?: 0
            if (h <= 0) continue

            val bucketHeight = when {
                h >= 1080 -> 1080
                h >= 720 -> 720
                h >= 480 -> 480
                else -> 360
            }

            if (!seenHeights.contains(bucketHeight)) {
                seenHeights.add(bucketHeight)

                val label = when (bucketHeight) {
                    1080 -> "Original (${format.width ?: 1080}p)"
                    720 -> "720p HD"
                    480 -> "480p SD"
                    else -> "360p"
                }

                val sizeBytes = calculateSizeBytes(format, durationSeconds)

                result.add(
                    InstagramFormat(
                        formatId = format.formatId ?: "mp4_$bucketHeight",
                        resolutionLabel = label,
                        width = format.width ?: 0,
                        height = format.height ?: bucketHeight,
                        ext = format.ext ?: "mp4",
                        fileSizeApprox = sizeBytes,
                        isAudioOnly = false
                    )
                )
            }
        }

        // Map best audio stream
        val bestAudio = audioStreams.maxByOrNull { it.abr ?: 0.0 } ?: rawFormats.firstOrNull { it.acodec != "none" && it.acodec != null }
        if (bestAudio != null) {
            val sizeBytes = calculateSizeBytes(bestAudio, durationSeconds)
            result.add(
                InstagramFormat(
                    formatId = bestAudio.formatId ?: "audio_best",
                    resolutionLabel = "Audio Only (MP3)",
                    width = 0,
                    height = 0,
                    ext = "mp3",
                    fileSizeApprox = sizeBytes,
                    isAudioOnly = true
                )
            )
        }

        return result
    }

    private fun calculateSizeBytes(format: Format, durationSeconds: Int): Long {
        if ((format.filesize ?: 0L) > 0L) return format.filesize!!
        if ((format.filesizeApprox ?: 0L) > 0L) return format.filesizeApprox!!

        val tbr = format.tbr ?: ((format.vbr ?: 0.0) + (format.abr ?: 0.0))
        if (tbr > 0.0 && durationSeconds > 0) {
            return ((tbr * 1024 / 8) * durationSeconds).toLong()
        }
        return 0L
    }

    private fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "00:00"
        val m = seconds / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d", m, s)
    }
}
