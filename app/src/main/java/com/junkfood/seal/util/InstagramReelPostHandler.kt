package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import java.util.regex.Pattern

/**
 * WP 2.6 — Instagram Reels Post Handler
 *
 * Handles Instagram Reel posts. Reels are short-form vertical videos (≤90s)
 * hosted at /reel/{shortcode}/ or /reels/{shortcode}/ URLs.
 * They are treated identically to VIDEO posts in the yt-dlp pipeline,
 * but annotated with REEL type for analytics and UI differentiation.
 */
object InstagramReelPostHandler {

    fun parseReelPostJson(jsonString: String, shortcode: String): InstagramMediaItem {
        val id = extractJsonString(jsonString, "id") ?: shortcode
        val url = extractJsonString(jsonString, "url") ?: ""
        val thumbnail = extractJsonString(jsonString, "thumbnail") ?: ""
        val uploader = extractJsonString(jsonString, "uploader") ?: "instagram_user"
        val title = extractJsonString(jsonString, "title") ?: ""
        val width = extractJsonInt(jsonString, "width") ?: 1080
        val height = extractJsonInt(jsonString, "height") ?: 1920
        val duration = extractJsonInt(jsonString, "duration") ?: 0

        return InstagramMediaItem(
            id = id,
            shortcode = shortcode,
            mediaType = InstagramMediaType.REEL,
            downloadUrl = url,
            thumbnailUrl = thumbnail,
            authorUsername = uploader,
            caption = title,
            width = width,
            height = height,
            isVideo = true,
            durationSeconds = duration
        )
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractJsonInt(json: String, key: String): Int? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*(\\d+)")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }
}
