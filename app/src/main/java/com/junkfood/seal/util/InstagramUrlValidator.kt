package com.junkfood.seal.util

import java.util.regex.Pattern

enum class InstagramUrlType {
    POST,
    REEL,
    STORY,
    HIGHLIGHT,
    PROFILE,
    UNKNOWN
}

data class InstagramUrlParseResult(
    val isValid: Boolean,
    val type: InstagramUrlType,
    val shortcode: String? = null,
    val username: String? = null,
    val rawUrl: String
)

object InstagramUrlValidator {

    private val REEL_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/(?:reel|reels|tv)/([A-Za-z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val POST_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/p/([A-Za-z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val STORY_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/stories/([A-Za-z0-9._-]+)/(\\d+)",
        Pattern.CASE_INSENSITIVE
    )

    private val HIGHLIGHT_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/stories/highlights/(\\d+)",
        Pattern.CASE_INSENSITIVE
    )

    private val PROFILE_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/([A-Za-z0-9._-]+)/?",
        Pattern.CASE_INSENSITIVE
    )

    fun parseUrl(url: String): InstagramUrlParseResult {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return InstagramUrlParseResult(isValid = false, type = InstagramUrlType.UNKNOWN, rawUrl = url)
        }

        // Check Reel
        val reelMatcher = REEL_PATTERN.matcher(trimmed)
        if (reelMatcher.find()) {
            return InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.REEL,
                shortcode = reelMatcher.group(1),
                rawUrl = trimmed
            )
        }

        // Check Post / Carousel
        val postMatcher = POST_PATTERN.matcher(trimmed)
        if (postMatcher.find()) {
            return InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.POST,
                shortcode = postMatcher.group(1),
                rawUrl = trimmed
            )
        }

        // Check Story Highlight
        val highlightMatcher = HIGHLIGHT_PATTERN.matcher(trimmed)
        if (highlightMatcher.find()) {
            return InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.HIGHLIGHT,
                shortcode = highlightMatcher.group(1),
                rawUrl = trimmed
            )
        }

        // Check Story
        val storyMatcher = STORY_PATTERN.matcher(trimmed)
        if (storyMatcher.find()) {
            return InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.STORY,
                username = storyMatcher.group(1),
                shortcode = storyMatcher.group(2),
                rawUrl = trimmed
            )
        }

        // Check Profile
        val profileMatcher = PROFILE_PATTERN.matcher(trimmed)
        if (profileMatcher.find()) {
            val user = profileMatcher.group(1)
            // Exclude static reserved paths
            if (user != null && !isReservedKeyword(user)) {
                return InstagramUrlParseResult(
                    isValid = true,
                    type = InstagramUrlType.PROFILE,
                    username = user,
                    rawUrl = trimmed
                )
            }
        }

        return InstagramUrlParseResult(isValid = false, type = InstagramUrlType.UNKNOWN, rawUrl = trimmed)
    }

    private fun isReservedKeyword(keyword: String): Boolean {
        val reserved = setOf("p", "reel", "reels", "tv", "stories", "explore", "direct", "accounts", "legal", "about")
        return reserved.contains(keyword.lowercase())
    }
}
