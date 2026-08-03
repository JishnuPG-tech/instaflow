package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaType

/**
 * WP 3.1 — Instagram Carousel Detector
 *
 * Detects whether an Instagram post URL/payload represents a carousel
 * (multi-image/video sidecar post, up to 10 items).
 *
 * Detection strategy:
 * 1. URL-based: carousels share the same /p/{shortcode}/ format as single posts.
 * 2. Payload-based: presence of "edge_sidecar_to_children" or "__typename": "GraphSidecar"
 *    in the yt-dlp JSON payload indicates a carousel.
 * 3. playlist_count > 1 in yt-dlp output indicates a carousel.
 */
object InstagramCarouselDetector {

    private const val SIDECAR_TYPENAME = "GraphSidecar"
    private const val SIDECAR_FIELD = "edge_sidecar_to_children"

    /**
     * Returns true if the yt-dlp JSON payload represents a carousel post.
     */
    fun isCarousel(jsonString: String): Boolean {
        return containsSidecarTypename(jsonString) ||
               containsSidecarField(jsonString) ||
               hasMultiplePlaylistEntries(jsonString)
    }

    /**
     * Returns the number of items in the carousel from playlist_count field.
     * Returns 1 if not a carousel or count cannot be determined.
     */
    fun detectCarouselCount(jsonString: String): Int {
        val pattern = java.util.regex.Pattern.compile("\"playlist_count\"\\s*:\\s*(\\d+)")
        val matcher = pattern.matcher(jsonString)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() ?: 1 else 1
    }

    /**
     * Infers the media type for a single carousel item based on its is_video flag.
     */
    fun inferCarouselItemType(isVideo: Boolean): InstagramMediaType {
        return if (isVideo) InstagramMediaType.VIDEO else InstagramMediaType.IMAGE
    }

    private fun containsSidecarTypename(json: String): Boolean {
        return json.contains("\"__typename\":\"$SIDECAR_TYPENAME\"") ||
               json.contains("\"__typename\": \"$SIDECAR_TYPENAME\"")
    }

    private fun containsSidecarField(json: String): Boolean {
        return json.contains(SIDECAR_FIELD)
    }

    private fun hasMultiplePlaylistEntries(json: String): Boolean {
        val count = detectCarouselCount(json)
        return count > 1
    }

    /**
     * Extracts the list of per-item JSON objects from a carousel yt-dlp payload.
     *
     * yt-dlp represents carousel items in the "entries" array of the parent JSON.
     * Each entry is a self-contained JSON object for one carousel item.
     *
     * Returns an empty list if no entries are found (caller should treat as parse failure).
     *
     * Note: This is a best-effort regex extraction — for production, the full JSON
     * is deserialized via kotlinx.serialization in the VideoInfo layer; this regex
     * path is used to retain JVM unit-test compatibility without Android framework stubs.
     */
    fun extractItemJsonList(json: String): List<String> {
        // Strategy: find the "entries" array and split into top-level JSON objects.
        val entriesPattern = Regex(""""entries"\s*:\s*\[""")
        val startMatch = entriesPattern.find(json) ?: return emptyList()

        val arrayStart = startMatch.range.last + 1  // index of '[' already consumed, so content starts here
        var depth = 1
        var i = arrayStart
        val entries = mutableListOf<String>()
        var objStart = -1

        while (i < json.length && depth > 0) {
            when (json[i]) {
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0 && objStart >= 0) {
                        entries.add(json.substring(objStart, i).trim().trimEnd(','))
                    }
                }
                '{' -> {
                    if (depth == 1 && objStart < 0) objStart = i
                    depth++
                }
                '}' -> {
                    depth--
                    if (depth == 1 && objStart >= 0) {
                        entries.add(json.substring(objStart, i + 1).trim().trimEnd(','))
                        objStart = -1
                    }
                }
            }
            i++
        }
        return entries
    }
}
