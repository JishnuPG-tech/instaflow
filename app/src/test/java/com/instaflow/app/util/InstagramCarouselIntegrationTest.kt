package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WP 3.10 — Carousel Integration Test
 *
 * End-to-end integration test for the full carousel pipeline:
 * Detection → Orchestration → Queue → Progress → Filename → Metadata → Notification
 *
 * Simulates a 4-item mixed carousel post (2 images + 2 videos).
 */
class InstagramCarouselIntegrationTest {

    private val rootJson = """{"__typename":"GraphSidecar","playlist_count":4}"""

    private val item0Json = """{"id":"c_img_0","url":"https://cdn.ig.com/img0.jpg","thumbnail":"https://cdn.ig.com/t0.jpg","uploader":"photouser","is_video":false,"width":1080,"height":1080}"""
    private val item1Json = """{"id":"c_vid_1","url":"https://cdn.ig.com/vid1.mp4","thumbnail":"https://cdn.ig.com/t1.jpg","uploader":"photouser","is_video":true,"width":1080,"height":1920,"duration":15}"""
    private val item2Json = """{"id":"c_img_2","url":"https://cdn.ig.com/img2.jpg","thumbnail":"https://cdn.ig.com/t2.jpg","uploader":"photouser","is_video":false,"width":1080,"height":1080}"""
    private val item3Json = """{"id":"c_vid_3","url":"https://cdn.ig.com/vid3.mp4","thumbnail":"https://cdn.ig.com/t3.jpg","uploader":"photouser","is_video":true,"width":1080,"height":1920,"duration":8}"""

    private val parentShortcode = "IntegTestShrt"
    private val parentCaption = "Full pipeline test post"
    private val authorUsername = "photouser"

    @Test
    fun testFullCarouselPipeline() {

        // Step 1: Detection
        assertTrue(InstagramCarouselDetector.isCarousel(rootJson))
        assertEquals(4, InstagramCarouselDetector.detectCarouselCount(rootJson))
        assertTrue(InstagramCarouselOrchestrator.shouldUseCarouselPipeline(rootJson))

        // Step 2: Orchestration
        val items = InstagramCarouselOrchestrator.orchestrate(
            parentShortcode = parentShortcode,
            itemJsonList = listOf(item0Json, item1Json, item2Json, item3Json),
            parentCaption = parentCaption,
            authorUsername = authorUsername
        )
        assertEquals(4, items.size)
        assertEquals(InstagramMediaType.IMAGE, items[0].mediaType)
        assertEquals(InstagramMediaType.VIDEO, items[1].mediaType)
        assertEquals(15, items[1].durationSeconds)
        assertEquals(parentCaption, items[0].caption)

        // Step 3: Queue
        val queue = InstagramCarouselQueueBuilder.buildQueue(items)
        assertEquals(4, queue.size)
        assertEquals("Item 1 of 4", InstagramCarouselQueueBuilder.queueLabel(queue[0]))
        assertEquals(2, InstagramCarouselQueueBuilder.videoItemsOnly(queue).size)
        assertEquals(2, InstagramCarouselQueueBuilder.imageItemsOnly(queue).size)

        // Step 4: Progress Tracker
        val tracker = InstagramCarouselProgressTracker(4)
        tracker.updateItemState(0, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        tracker.updateItemState(1, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        assertEquals(0.5f, tracker.aggregateProgress(), 0.001f)
        tracker.updateItemState(2, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        tracker.updateItemState(3, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        assertTrue(tracker.isFullyComplete())

        // Step 5: Filenames
        assertEquals("${parentShortcode}_1_of_4.jpg", InstagramCarouselFilenameStrategy.generateFilename(items[0]))
        assertEquals("${parentShortcode}_2_of_4.mp4", InstagramCarouselFilenameStrategy.generateFilename(items[1]))
        assertEquals(parentShortcode, InstagramCarouselFilenameStrategy.carouselDirectoryName(items[0]))

        // Step 6: Metadata Aggregation
        val summary = InstagramCarouselMetadataAggregator.aggregate(items)
        assertEquals(4, summary.totalItems)
        assertEquals(2, summary.imageCount)
        assertEquals(2, summary.videoCount)
        assertEquals(23, summary.totalDurationSeconds)
        assertTrue(summary.hasMixedContent)
        val desc = InstagramCarouselMetadataAggregator.describeCarousel(summary)
        assertEquals("4 items (2 photos, 2 videos)", desc)

        // Step 7: Notifications
        val startNotif = InstagramCarouselNotificationHandler.buildStartNotification(parentShortcode, 4, authorUsername)
        val progressNotif = InstagramCarouselNotificationHandler.buildProgressNotification(parentShortcode, authorUsername, 2, 4)
        val successNotif = InstagramCarouselNotificationHandler.buildSuccessNotification(parentShortcode, authorUsername, 4)

        assertEquals(startNotif.notificationId, progressNotif.notificationId)
        assertEquals(startNotif.notificationId, successNotif.notificationId)
        assertEquals(50, progressNotif.progressPercent)
        assertEquals(100, successNotif.progressPercent)
        assertFalse(successNotif.isOngoing)
    }
}
