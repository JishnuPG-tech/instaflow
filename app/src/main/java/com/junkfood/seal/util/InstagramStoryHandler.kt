package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import java.util.regex.Pattern

/**
 * WP 2.7 — Instagram Stories Handler
 *
 * Handles Instagram Story media (both photo and video stories).
 * Stories are accessed at /stories/{username}/{story_id}/ URLs.
 * The isVideo flag is determined from the JSON payload's "is_video" field.
 */
object InstagramStoryHandler {

    fun parseStoryJson(jsonString: String, shortcode: String): InstagramMediaItem {
        val id = extractJsonString(jsonString, "id") ?: shortcode
        val url = extractJsonString(jsonString, "url") ?: ""
        val thumbnail = extractJsonString(jsonString, "thumbnail") ?: ""
        val uploader = extractJsonString(jsonString, "uploader") ?: "instagram_user"
        val isVideoRaw = extractJsonBoolean(jsonString, "is_video") ?: false
        val width = extractJsonInt(jsonString, "width") ?: 1080
        val height = extractJsonInt(jsonString, "height") ?: 1920
        val duration = extractJsonInt(jsonString, "duration") ?: 0

        return InstagramMediaItem(
            id = id,
            shortcode = shortcode,
            mediaType = InstagramMediaType.STORY,
            downloadUrl = url,
            thumbnailUrl = thumbnail,
            authorUsername = uploader,
            caption = null,
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
