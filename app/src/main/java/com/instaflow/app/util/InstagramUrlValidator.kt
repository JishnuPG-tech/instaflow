package com.instaflow.app.util

import android.util.Log
import java.util.regex.Pattern

enum class InstagramUrlType {
    POST,
    REEL,
    STORY,
    HIGHLIGHT,
    PROFILE_PIC,
    CAROUSEL,
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

    private const val TAG = "InstagramUrlValidator"

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

    private val HIGHLIGHT_S_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/s/([A-Za-z0-9_-]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val PROFILE_PATTERN = Pattern.compile(
        "https?://(?:www\\.)?instagram\\.com/([A-Za-z0-9._-]+)/?",
        Pattern.CASE_INSENSITIVE
    )

    fun parseUrl(url: String): InstagramUrlParseResult {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            Log.w(TAG, "[UrlValidator] Input URL is empty")
            return InstagramUrlParseResult(isValid = false, type = InstagramUrlType.UNKNOWN, rawUrl = url)
        }

        // Check Reel
        val reelMatcher = REEL_PATTERN.matcher(trimmed)
        if (reelMatcher.find()) {
            val res = InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.REEL,
                shortcode = reelMatcher.group(1),
                rawUrl = trimmed
            )
            Log.i(TAG, "[UrlValidator] Matched REEL -> shortcode=${res.shortcode}, rawUrl=$trimmed")
            return res
        }

        // Check Post / Carousel
        val postMatcher = POST_PATTERN.matcher(trimmed)
        if (postMatcher.find()) {
            val res = InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.POST,
                shortcode = postMatcher.group(1),
                rawUrl = trimmed
            )
            Log.i(TAG, "[UrlValidator] Matched POST -> shortcode=${res.shortcode}, rawUrl=$trimmed")
            return res
        }

        // Check Story Highlight
        val highlightMatcher = HIGHLIGHT_PATTERN.matcher(trimmed)
        if (highlightMatcher.find()) {
            val res = InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.HIGHLIGHT,
                shortcode = highlightMatcher.group(1),
                rawUrl = trimmed
            )
            Log.i(TAG, "[UrlValidator] Matched HIGHLIGHT -> shortcode=${res.shortcode}, rawUrl=$trimmed")
            return res
        }

        // Check Story Highlight (Short Link)
        val highlightSMatcher = HIGHLIGHT_S_PATTERN.matcher(trimmed)
        if (highlightSMatcher.find()) {
            val res = InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.HIGHLIGHT,
                shortcode = highlightSMatcher.group(1),
                rawUrl = trimmed
            )
            Log.i(TAG, "[UrlValidator] Matched HIGHLIGHT (S) -> shortcode=${res.shortcode}, rawUrl=$trimmed")
            return res
        }

        // Check Story
        val storyMatcher = STORY_PATTERN.matcher(trimmed)
        if (storyMatcher.find()) {
            val res = InstagramUrlParseResult(
                isValid = true,
                type = InstagramUrlType.STORY,
                username = storyMatcher.group(1),
                shortcode = storyMatcher.group(2),
                rawUrl = trimmed
            )
            Log.i(TAG, "[UrlValidator] Matched STORY -> user=${res.username}, shortcode=${res.shortcode}, rawUrl=$trimmed")
            return res
        }

        // Check Profile
        val profileMatcher = PROFILE_PATTERN.matcher(trimmed)
        if (profileMatcher.find()) {
            val user = profileMatcher.group(1)
            // Exclude static reserved paths
            if (user != null && !isReservedKeyword(user)) {
                val res = InstagramUrlParseResult(
                    isValid = true,
                    type = InstagramUrlType.PROFILE_PIC,
                    username = user,
                    rawUrl = trimmed
                )
                Log.i(TAG, "[UrlValidator] Matched PROFILE_PIC -> user=${res.username}, rawUrl=$trimmed")
                return res
            }
        }

        Log.w(TAG, "[UrlValidator] Unrecognized Instagram URL format: $trimmed")
        return InstagramUrlParseResult(isValid = false, type = InstagramUrlType.UNKNOWN, rawUrl = trimmed)
    }

    private fun isReservedKeyword(keyword: String): Boolean {
        val reserved = setOf("p", "reel", "reels", "tv", "stories", "explore", "direct", "accounts", "legal", "about")
        return reserved.contains(keyword.lowercase())
    }
}

