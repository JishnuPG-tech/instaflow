package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType

/**
 * WP 3.7 — Instagram Carousel Metadata Aggregator
 *
 * Aggregates metadata across all items in a carousel post.
 * Produces a summary useful for notifications, history entries, and database records.
 */
object InstagramCarouselMetadataAggregator {

    data class CarouselSummary(
        val shortcode: String,
        val authorUsername: String,
        val caption: String?,
        val totalItems: Int,
        val imageCount: Int,
        val videoCount: Int,
        val totalDurationSeconds: Int,
        val hasVideo: Boolean,
        val hasMixedContent: Boolean,
        val thumbnailUrl: String   // thumbnail of first item
    )

    fun aggregate(items: List<InstagramMediaItem>): CarouselSummary {
        require(items.isNotEmpty()) { "Cannot aggregate empty carousel item list" }

        val first = items.first()
        val imageCount = items.count { !it.isVideo }
        val videoCount = items.count { it.isVideo }
        val totalDuration = items.sumOf { it.durationSeconds }

        return CarouselSummary(
            shortcode = first.shortcode,
            authorUsername = first.authorUsername,
            caption = first.caption,
            totalItems = items.size,
            imageCount = imageCount,
            videoCount = videoCount,
            totalDurationSeconds = totalDuration,
            hasVideo = videoCount > 0,
            hasMixedContent = imageCount > 0 && videoCount > 0,
            thumbnailUrl = first.thumbnailUrl
        )
    }

    /**
     * Returns a human-readable description of the carousel content.
     * E.g. "5 items (3 photos, 2 videos)"
     */
    fun describeCarousel(summary: CarouselSummary): String {
        return buildString {
            append("${summary.totalItems} items (")
            if (summary.imageCount > 0) append("${summary.imageCount} photo${if (summary.imageCount > 1) "s" else ""}")
            if (summary.hasMixedContent) append(", ")
            if (summary.videoCount > 0) append("${summary.videoCount} video${if (summary.videoCount > 1) "s" else ""}")
            append(")")
        }
    }
}
