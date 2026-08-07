package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaItem
import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselQueueBuilderTest {

    private fun makeImageItem(index: Int, total: Int) = InstagramMediaItem(
        id = "img_$index",
        shortcode = "CarouselAbc",
        mediaType = InstagramMediaType.IMAGE,
        downloadUrl = "https://cdn.instagram.com/img_$index.jpg",
        thumbnailUrl = "https://cdn.instagram.com/thumb_$index.jpg",
        authorUsername = "testuser",
        isVideo = false,
        carouselIndex = index,
        totalCarouselItems = total
    )

    private fun makeVideoItem(index: Int, total: Int) = InstagramMediaItem(
        id = "vid_$index",
        shortcode = "CarouselAbc",
        mediaType = InstagramMediaType.VIDEO,
        downloadUrl = "https://cdn.instagram.com/vid_$index.mp4",
        thumbnailUrl = "https://cdn.instagram.com/vthumb_$index.jpg",
        authorUsername = "testuser",
        isVideo = true,
        carouselIndex = index,
        totalCarouselItems = total
    )

    @Test
    fun testBuildQueueOrderAndLabels() {
        val items = listOf(makeImageItem(0, 3), makeVideoItem(1, 3), makeImageItem(2, 3))
        val queue = InstagramCarouselQueueBuilder.buildQueue(items)

        assertEquals(3, queue.size)
        assertEquals(1, queue[0].queuePosition)
        assertEquals(3, queue[0].totalInQueue)
        assertEquals("Item 1 of 3", InstagramCarouselQueueBuilder.queueLabel(queue[0]))
        assertEquals("Item 3 of 3", InstagramCarouselQueueBuilder.queueLabel(queue[2]))
    }

    @Test
    fun testParallelFlag() {
        val items = listOf(makeImageItem(0, 2), makeImageItem(1, 2))
        val parallelQueue = InstagramCarouselQueueBuilder.buildQueue(items, allowParallel = true)
        val serialQueue = InstagramCarouselQueueBuilder.buildQueue(items, allowParallel = false)

        assertTrue(parallelQueue.all { it.canParallelize })
        assertFalse(serialQueue.any { it.canParallelize })
    }

    @Test
    fun testFilterVideoAndImageItems() {
        val items = listOf(makeImageItem(0, 4), makeVideoItem(1, 4), makeImageItem(2, 4), makeVideoItem(3, 4))
        val queue = InstagramCarouselQueueBuilder.buildQueue(items)

        val videos = InstagramCarouselQueueBuilder.videoItemsOnly(queue)
        val images = InstagramCarouselQueueBuilder.imageItemsOnly(queue)

        assertEquals(2, videos.size)
        assertEquals(2, images.size)
        assertTrue(videos.all { it.item.isVideo })
        assertFalse(images.any { it.item.isVideo })
    }
}
