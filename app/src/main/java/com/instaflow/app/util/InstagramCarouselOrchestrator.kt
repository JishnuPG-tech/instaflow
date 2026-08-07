package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaItem
import com.instaflow.app.database.InstagramMediaType

/**
 * WP 3.3 — Instagram Carousel Orchestrator
 *
 * Orchestrates the full carousel download pipeline:
 * 1. Detect whether the post is a carousel.
 * 2. Determine item count.
 * 3. Parse each item into InstagramMediaItem.
 * 4. Return ordered list of items for the download queue.
 *
 * This is a pure data-transformation layer with no I/O.
 */
object InstagramCarouselOrchestrator {

    /**
     * Given raw yt-dlp JSON payloads for a carousel post, returns an ordered list of
     * InstagramMediaItem. If only one payload is supplied and no carousel is detected,
     * returns a single-item list with CAROUSEL type to preserve future extensibility.
     *
     * @param parentShortcode The shortcode of the parent carousel post.
     * @param itemJsonList Ordered list of yt-dlp JSON strings, one per carousel slot.
     * @param parentCaption Optional caption from the parent post.
     * @param authorUsername Author of the parent post.
     */
    fun orchestrate(
        parentShortcode: String,
        itemJsonList: List<String>,
        parentCaption: String? = null,
        authorUsername: String = "instagram_user"
    ): List<InstagramMediaItem> {
        val total = itemJsonList.size
        return itemJsonList.mapIndexed { index, json ->
            val item = InstagramCarouselItemParser.parseCarouselItem(
                jsonString = json,
                parentShortcode = parentShortcode,
                index = index,
                totalItems = total
            )
            // Attach parent caption and authorUsername if not already extracted from item JSON
            item.copy(
                caption = parentCaption,
                authorUsername = if (item.authorUsername == "instagram_user") authorUsername else item.authorUsername
            )
        }
    }

    /**
     * Returns true if the post should be routed through the carousel pipeline.
     */
    fun shouldUseCarouselPipeline(rootJson: String): Boolean {
        return InstagramCarouselDetector.isCarousel(rootJson)
    }
}
