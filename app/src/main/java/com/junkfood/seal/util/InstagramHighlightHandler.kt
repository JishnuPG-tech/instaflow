package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import java.util.regex.Pattern

/**
 * WP 2.8 — Instagram Story Highlights Handler
 *
 * Handles Instagram Story Highlights, which are curated collections of stories
 * accessible at /stories/highlights/{highlight_id}/ URLs.
 * Each highlight is a playlist; individual items are treated as STORY type.
 * The highlight itself uses REEL type as the container.
 */
object InstagramHighlightHandler {

    fun parseHighlightJson(jsonString: String, highlightId: String): InstagramMediaItem {
        val id = extractJsonString(jsonString, "id") ?: highlightId
        val url = extractJsonString(jsonString, "url") ?: ""
        val thumbnail = extractJsonString(jsonString, "thumbnail") ?: ""
        val uploader = extractJsonString(jsonString, "uploader") ?: "instagram_user"
        val title = extractJsonString(jsonString, "title") ?: ""
        val isVideoRaw = extractJsonBoolean(jsonString, "is_video") ?: false
        val width = extractJsonInt(jsonString, "width") ?: 1080
        val height = extractJsonInt(jsonString, "height") ?: 1920
        val duration = extractJsonInt(jsonString, "duration") ?: 0

        return InstagramMediaItem(
            id = id,
            shortcode = highlightId,
            mediaType = InstagramMediaType.STORY,
            downloadUrl = url,
            thumbnailUrl = thumbnail,
            authorUsername = uploader,
            caption = title,
            width = width,
            height = height,
            isVideo = isVideoRaw,
            durationSeconds = duration
        )
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractJsonBoolean(json: String, key: String): Boolean? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*(true|false)")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1) == "true" else null
    }

    private fun extractJsonInt(json: String, key: String): Int? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*(\\d+)")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }
}
