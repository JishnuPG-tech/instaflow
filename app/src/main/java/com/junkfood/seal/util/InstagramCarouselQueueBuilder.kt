package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem

/**
 * WP 3.4 — Instagram Carousel Download Queue Builder
 *
 * Converts an ordered list of InstagramMediaItem (from the Carousel Orchestrator)
 * into a prioritized download queue. Each queue entry records:
 * - The item to download
 * - Its queue position (1-based, human-readable)
 * - Whether it can be downloaded in parallel
 *
 * The queue is serializable for persistence across process restarts.
 */
object InstagramCarouselQueueBuilder {

    data class CarouselQueueEntry(
        val item: InstagramMediaItem,
        val queuePosition: Int,    // 1-based
        val totalInQueue: Int,
        val canParallelize: Boolean = true
    )

    /**
     * Builds an ordered download queue from a list of carousel items.
     * Items are enqueued in carousel index order.
     * @param allowParallel If true, marks all entries as parallelizable.
     */
    fun buildQueue(
        items: List<InstagramMediaItem>,
        allowParallel: Boolean = true
    ): List<CarouselQueueEntry> {
        val total = items.size
        return items.mapIndexed { index, item ->
            CarouselQueueEntry(
                item = item,
                queuePosition = index + 1,
                totalInQueue = total,
                canParallelize = allowParallel
            )
        }
    }

    /**
     * Returns a human-readable label for a queue entry, e.g. "Item 3 of 10".
     */
    fun queueLabel(entry: CarouselQueueEntry): String {
        return "Item ${entry.queuePosition} of ${entry.totalInQueue}"
    }

    /**
     * Filters the queue to only video items.
     */
    fun videoItemsOnly(queue: List<CarouselQueueEntry>): List<CarouselQueueEntry> {
        return queue.filter { it.item.isVideo }
    }

    /**
     * Filters the queue to only image items.
     */
    fun imageItemsOnly(queue: List<CarouselQueueEntry>): List<CarouselQueueEntry> {
        return queue.filter { !it.item.isVideo }
    }
}
