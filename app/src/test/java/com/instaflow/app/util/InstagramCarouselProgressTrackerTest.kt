package com.instaflow.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselProgressTrackerTest {

    @Test
    fun testInitialStateIsPending() {
        val tracker = InstagramCarouselProgressTracker(3)
        assertEquals(InstagramCarouselProgressTracker.ItemState.PENDING, tracker.getItemState(0))
        assertEquals(0f, tracker.aggregateProgress(), 0.001f)
        assertFalse(tracker.isFullyComplete())
    }

    @Test
    fun testProgressUpdates() {
        val tracker = InstagramCarouselProgressTracker(2)
        tracker.updateItemProgress(0, 0.5f)
        tracker.updateItemProgress(1, 1.0f)

        assertEquals(0.75f, tracker.aggregateProgress(), 0.001f)
        assertEquals(1, tracker.completedCount())
        assertFalse(tracker.isFullyComplete())
    }

    @Test
    fun testFullCompletion() {
        val tracker = InstagramCarouselProgressTracker(3)
        tracker.updateItemState(0, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        tracker.updateItemState(1, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        tracker.updateItemState(2, InstagramCarouselProgressTracker.ItemState.COMPLETED)

        assertTrue(tracker.isFullyComplete())
        assertEquals(3, tracker.completedCount())
        assertEquals(1f, tracker.aggregateProgress(), 0.001f)
    }

    @Test
    fun testFailureTracking() {
        val tracker = InstagramCarouselProgressTracker(3)
        tracker.updateItemState(0, InstagramCarouselProgressTracker.ItemState.COMPLETED)
        tracker.updateItemState(1, InstagramCarouselProgressTracker.ItemState.FAILED)

        assertFalse(tracker.isFullyComplete())
        assertTrue(tracker.hasAnyFailure())
        assertEquals(1, tracker.failedCount())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidIndexThrows() {
        val tracker = InstagramCarouselProgressTracker(3)
        tracker.updateItemProgress(5, 0.5f)
    }
}
