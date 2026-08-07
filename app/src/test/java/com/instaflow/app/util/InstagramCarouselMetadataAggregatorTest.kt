package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaItem
import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselMetadataAggregatorTest {

    private fun makeItem(isVideo: Boolean, duration: Int = 0, index: Int = 0) = InstagramMediaItem(
        id = "item_$index",
        shortcode = "CarShortXYZ",
        mediaType = if (isVideo) InstagramMediaType.VIDEO else InstagramMediaType.IMAGE,
        downloadUrl = "https://cdn.instagram.com/item.jpg",
        thumbnailUrl = "https://cdn.instagram.com/thumb.jpg",
        authorUsername = "testauthor",
        caption = "Test caption",
        isVideo = isVideo,
        durationSeconds = duration,
        carouselIndex = index,
        totalCarouselItems = 4
    )

    @Test
    fun testAggregateMixedCarousel() {
        val items = listOf(
            makeItem(false, 0, 0),
            makeItem(true, 12, 1),
            makeItem(false, 0, 2),
            makeItem(true, 8, 3)
        )
        val summary = InstagramCarouselMetadataAggregator.aggregate(items)

        assertEquals("CarShortXYZ", summary.shortcode)
        assertEquals(4, summary.totalItems)
        assertEquals(2, summary.imageCount)
        assertEquals(2, summary.videoCount)
        assertEquals(20, summary.totalDurationSeconds)
        assertTrue(summary.hasVideo)
        assertTrue(summary.hasMixedContent)
    }

    @Test
    fun testAggregateImagesOnly() {
        val items = listOf(makeItem(false, 0, 0), makeItem(false, 0, 1))
        val summary = InstagramCarouselMetadataAggregator.aggregate(items)

        assertFalse(summary.hasVideo)
        assertFalse(summary.hasMixedContent)
        assertEquals(2, summary.imageCount)
        assertEquals(0, summary.videoCount)
    }

    @Test
    fun testDescribeCarousel() {
        val items = listOf(makeItem(false, 0, 0), makeItem(true, 5, 1), makeItem(false, 0, 2))
        val summary = InstagramCarouselMetadataAggregator.aggregate(items)
        val desc = InstagramCarouselMetadataAggregator.describeCarousel(summary)

        assertEquals("3 items (2 photos, 1 video)", desc)
    }
}
