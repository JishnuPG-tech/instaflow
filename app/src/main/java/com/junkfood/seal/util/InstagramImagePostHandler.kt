package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import java.util.regex.Pattern

object InstagramImagePostHandler {

    fun parseImagePostJson(jsonString: String, shortcode: String): InstagramMediaItem {
        val id = extractJsonString(jsonString, "id") ?: shortcode
        val url = extractJsonString(jsonString, "url") ?: ""
        val thumbnail = extractJsonString(jsonString, "thumbnail") ?: url
        val uploader = extractJsonString(jsonString, "uploader") ?: "instagram_user"
        val title = extractJsonString(jsonString, "title") ?: ""
        val width = extractJsonInt(jsonString, "width") ?: 1080
        val height = extractJsonInt(jsonString, "height") ?: 1080

        return InstagramMediaItem(
            id = id,
            shortcode = shortcode,
            mediaType = InstagramMediaType.IMAGE,
            downloadUrl = url,
            thumbnailUrl = thumbnail,
            authorUsername = uploader,
            caption = title,
            width = width,
            height = height,
            isVideo = false
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
