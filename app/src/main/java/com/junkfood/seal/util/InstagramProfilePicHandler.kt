package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import java.util.regex.Pattern

/**
 * WP 2.9 — Instagram Profile Picture Handler
 *
 * Handles Instagram profile picture downloads.
 * Accessed at /{username}/ or /p/{shortcode}/ with profile_pic_url_hd field.
 * Always IMAGE type, never a video.
 */
object InstagramProfilePicHandler {

    fun parseProfilePicJson(jsonString: String, username: String): InstagramMediaItem {
        val id = extractJsonString(jsonString, "id") ?: username
        val url = extractJsonString(jsonString, "url") ?: ""
        val thumbnail = extractJsonString(jsonString, "thumbnail") ?: url

        return InstagramMediaItem(
            id = id,
            shortcode = username,
            mediaType = InstagramMediaType.PROFILE_PIC,
            downloadUrl = url,
            thumbnailUrl = thumbnail,
            authorUsername = username,
            caption = null,
            width = 0,
            height = 0,
            isVideo = false
        )
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*\"([^\"]+)\"")
        val matcher = pattern.matcher(json)
        return if (matcher.find()) matcher.group(1) else null
    }
}
