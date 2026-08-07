package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaItem
import com.instaflow.app.database.InstagramMediaType

/**
 * WP 3.6 — Instagram Carousel Filename Strategy
 *
 * Generates deterministic, collision-free filenames for carousel item downloads.
 * Naming convention:
 *   {shortcode}_{index+1}_of_{total}.{ext}
 *   e.g. CaRoUsEl001_1_of_5.jpg, CaRoUsEl001_2_of_5.mp4
 *
 * This ensures:
 * - Items sort correctly in file explorers.
 * - No filename collision between carousel items.
 * - Extension reflects true media type.
 */
object InstagramCarouselFilenameStrategy {

    fun generateFilename(item: InstagramMediaItem): String {
        val position = item.carouselIndex + 1
        val total = item.totalCarouselItems
        val ext = extensionFor(item)
        return "${item.shortcode}_${position}_of_${total}.$ext"
    }

    fun generateFilenameWithAuthor(item: InstagramMediaItem): String {
        val position = item.carouselIndex + 1
        val total = item.totalCarouselItems
        val ext = extensionFor(item)
        val safeAuthor = item.authorUsername.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return "${safeAuthor}_${item.shortcode}_${position}_of_${total}.$ext"
    }

    fun extensionFor(item: InstagramMediaItem): String {
        return when {
            item.isVideo -> "mp4"
            item.mediaType == InstagramMediaType.PROFILE_PIC -> "jpg"
            else -> "jpg"
        }
    }

    /**
     * Generates a subdirectory name for grouping all items in one carousel download.
     */
    fun carouselDirectoryName(item: InstagramMediaItem): String {
        return item.shortcode
    }
}
