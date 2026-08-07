package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaItem

/**
 * WP 3.9 — Instagram Carousel Notification Handler
 *
 * Generates notification content for carousel download events.
 * This is a pure data layer — no Android notification APIs are imported,
 * ensuring full JVM unit test compatibility.
 *
 * The UI layer (ViewModel/Worker) is responsible for posting the actual notification.
 */
object InstagramCarouselNotificationHandler {

    data class NotificationContent(
        val notificationId: Int,
        val title: String,
        val body: String,
        val progressPercent: Int,    // 0–100, -1 = indeterminate
        val isOngoing: Boolean,
        val isCancelable: Boolean
    )

    fun buildStartNotification(
        shortcode: String,
        totalItems: Int,
        authorUsername: String
    ): NotificationContent {
        return NotificationContent(
            notificationId = shortcode.hashCode(),
            title = "Downloading carousel from @$authorUsername",
            body = "Starting download of $totalItems items...",
            progressPercent = 0,
            isOngoing = true,
            isCancelable = true
        )
    }

    fun buildProgressNotification(
        shortcode: String,
        authorUsername: String,
        completedItems: Int,
        totalItems: Int
    ): NotificationContent {
        val percent = ((completedItems.toFloat() / totalItems) * 100).toInt()
        return NotificationContent(
            notificationId = shortcode.hashCode(),
            title = "Downloading carousel from @$authorUsername",
            body = "Item $completedItems of $totalItems downloaded",
            progressPercent = percent,
            isOngoing = true,
            isCancelable = true
        )
    }

    fun buildSuccessNotification(
        shortcode: String,
        authorUsername: String,
        totalItems: Int
    ): NotificationContent {
        return NotificationContent(
            notificationId = shortcode.hashCode(),
            title = "Carousel downloaded",
            body = "All $totalItems items from @$authorUsername saved",
            progressPercent = 100,
            isOngoing = false,
            isCancelable = false
        )
    }

    fun buildPartialFailureNotification(
        shortcode: String,
        authorUsername: String,
        failedCount: Int,
        totalItems: Int
    ): NotificationContent {
        return NotificationContent(
            notificationId = shortcode.hashCode(),
            title = "Carousel download incomplete",
            body = "$failedCount of $totalItems items failed from @$authorUsername",
            progressPercent = 100,
            isOngoing = false,
            isCancelable = false
        )
    }
}
