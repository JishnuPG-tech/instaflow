package com.junkfood.seal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselNotificationHandlerTest {

    @Test
    fun testStartNotification() {
        val notif = InstagramCarouselNotificationHandler.buildStartNotification(
            shortcode = "AbCdEfG", totalItems = 5, authorUsername = "testuser"
        )
        assertEquals(0, notif.progressPercent)
        assertTrue(notif.isOngoing)
        assertTrue(notif.isCancelable)
        assertTrue(notif.title.contains("testuser"))
        assertTrue(notif.body.contains("5"))
    }

    @Test
    fun testProgressNotification() {
        val notif = InstagramCarouselNotificationHandler.buildProgressNotification(
            shortcode = "AbCdEfG", authorUsername = "testuser",
            completedItems = 3, totalItems = 5
        )
        assertEquals(60, notif.progressPercent)
        assertTrue(notif.isOngoing)
        assertTrue(notif.body.contains("3") && notif.body.contains("5"))
    }

    @Test
    fun testSuccessNotification() {
        val notif = InstagramCarouselNotificationHandler.buildSuccessNotification(
            shortcode = "AbCdEfG", authorUsername = "testuser", totalItems = 5
        )
        assertEquals(100, notif.progressPercent)
        assertFalse(notif.isOngoing)
        assertFalse(notif.isCancelable)
        assertTrue(notif.body.contains("5"))
    }

    @Test
    fun testPartialFailureNotification() {
        val notif = InstagramCarouselNotificationHandler.buildPartialFailureNotification(
            shortcode = "AbCdEfG", authorUsername = "testuser",
            failedCount = 2, totalItems = 5
        )
        assertFalse(notif.isOngoing)
        assertTrue(notif.title.contains("incomplete"))
        assertTrue(notif.body.contains("2") && notif.body.contains("5"))
    }

    @Test
    fun testNotificationIdIsConsistent() {
        val id1 = InstagramCarouselNotificationHandler.buildStartNotification("XyZ123", 3, "u").notificationId
        val id2 = InstagramCarouselNotificationHandler.buildSuccessNotification("XyZ123", "u", 3).notificationId
        assertEquals(id1, id2)
    }
}
