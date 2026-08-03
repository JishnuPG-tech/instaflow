package com.junkfood.seal.util

/**
 * WP 3.5 — Instagram Carousel Progress Tracker
 *
 * Tracks download progress for each item in a carousel.
 * Provides aggregate progress (0.0–1.0) across the full carousel.
 * Thread-safe via synchronized access to the state map.
 */
class InstagramCarouselProgressTracker(private val totalItems: Int) {

    enum class ItemState { PENDING, DOWNLOADING, COMPLETED, FAILED }

    private val stateMap: MutableMap<Int, ItemState> = mutableMapOf()
    private val progressMap: MutableMap<Int, Float> = mutableMapOf()

    init {
        require(totalItems > 0) { "totalItems must be > 0" }
        for (i in 0 until totalItems) {
            stateMap[i] = ItemState.PENDING
            progressMap[i] = 0f
        }
    }

    @Synchronized
    fun updateItemState(index: Int, state: ItemState) {
        require(index in 0 until totalItems) { "index $index out of bounds" }
        stateMap[index] = state
        if (state == ItemState.COMPLETED) progressMap[index] = 1f
        if (state == ItemState.FAILED) progressMap[index] = 0f
    }

    @Synchronized
    fun updateItemProgress(index: Int, progress: Float) {
        require(index in 0 until totalItems)
        progressMap[index] = progress.coerceIn(0f, 1f)
        if (progress >= 1f) stateMap[index] = ItemState.COMPLETED
    }

    @Synchronized
    fun aggregateProgress(): Float {
        return progressMap.values.sum() / totalItems
    }

    @Synchronized
    fun completedCount(): Int = stateMap.values.count { it == ItemState.COMPLETED }

    @Synchronized
    fun failedCount(): Int = stateMap.values.count { it == ItemState.FAILED }

    @Synchronized
    fun isFullyComplete(): Boolean = completedCount() == totalItems

    @Synchronized
    fun hasAnyFailure(): Boolean = failedCount() > 0

    @Synchronized
    fun getItemState(index: Int): ItemState = stateMap[index] ?: ItemState.PENDING
}
