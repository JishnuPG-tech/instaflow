package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaItem
import com.instaflow.app.database.InstagramMediaType
import java.util.regex.Pattern

/**
 * WP 3.2 — Instagram Carousel Item Parser
 *
 * Parses individual items within a carousel post. Each item has its own
 * URL, thumbnail, dimensions, and video flag. Items are indexed 0-based
 * and the parser produces one InstagramMediaItem per carousel slot.
 */
object InstagramCarouselItemParser {

    /**
     * Parses a single carousel item payload (one entry from yt-dlp entries array).
     * @param jsonString The JSON block for a single carousel entry.
     * @param parentShortcode The shortcode of the parent carousel post.
     * @param index 0-based index of this item within the carousel.
     * @param totalItems Total number of items in the carousel.
     */
    fun parseCarouselItem(
        jsonString: String,
        parentShortcode: String,
        index: Int,
        totalItems: Int
    ): InstagramMediaItem {
        val id = extractJsonString(jsonString, "id") ?: "${parentShortcode}_${index}"
        val url = extractJsonString(jsonString, "url") ?: ""
        val thumbnail = extractJsonString(jsonString, "thumbnail") ?: ""
        val uploader = extractJsonString(jsonString, "uploader") ?: "instagram_user"
        val isVideoRaw = extractJsonBoolean(jsonString, "is_video") ?: false
        val width = extractJsonInt(jsonString, "width") ?: 1080
        val height = extractJsonInt(jsonString, "height") ?: 1080
        val duration = extractJsonInt(jsonString, "duration") ?: 0

        val mediaType = if (isVideoRaw) InstagramMediaType.VIDEO else InstagramMediaType.IMAGE

        return InstagramMediaItem(
            id = id,
            shortcode = parentShortcode,
            mediaType = mediaType,
            downloadUrl = url,
            thumbnailUrl = thumbnail,
            authorUsername = uploader,
            caption = null,
            width = width,
            height = height,
            isVideo = isVideoRaw,
            durationSeconds = duration,
            carouselIndex = index,
            totalCarouselItems = totalItems
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
