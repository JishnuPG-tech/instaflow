package com.instaflow.app.util

import android.util.Log
import com.instaflow.app.download.DownloaderV2
import com.instaflow.app.download.Task

/**
 * WP 4.2 — Instagram Carousel Router
 *
 * Single integration point bridging the Phase 3 carousel utility layer
 * to the live DownloaderV2 pipeline.
 *
 * ## Two entry points
 *
 * 1. [routeFromPlaylist] — Production path.
 *    Called when DownloadUtil.getPlaylistOrVideoInfo() returns a [PlaylistResult]
 *    whose extractorKey is "Instagram". This is how yt-dlp naturally represents
 *    carousel posts: as a playlist with N entries.
 *
 * 2. [route] — Fallback path (raw-JSON based).
 *    Used when only the raw yt-dlp JSON string is available (e.g. custom command
 *    output). Kept for completeness and unit-test compatibility.
 *
 * This class performs NO network I/O. All inputs must already be resolved.
 */
object InstagramCarouselRouter {

    private const val TAG = "InstagramCarouselRouter"
    private const val INSTAGRAM_EXTRACTOR_KEY = "Instagram"
    private const val INSTAGRAM_HOST = "instagram.com"

    // -----------------------------------------------------------------------
    // Routing result
    // -----------------------------------------------------------------------

    sealed interface RoutingResult {
        /** The content is a single media item. Use the normal InstaFlow download path. */
        data object SingleItem : RoutingResult

        /** Carousel detected and all tasks enqueued into DownloaderV2. */
        data class CarouselEnqueued(
            val tasks: List<Task>,
            val itemCount: Int,
            val shortcode: String,
            val author: String,
        ) : RoutingResult

        /** Carousel detected but could not be processed. */
        data class ParseFailed(val reason: String) : RoutingResult
    }

    // -----------------------------------------------------------------------
    // Entry point 1: PlaylistResult path (production)
    // -----------------------------------------------------------------------

    /**
     * Routes a [PlaylistResult] returned by [DownloadUtil.getPlaylistOrVideoInfo].
     *
     * Call this when the result has `extractorKey == "Instagram"` and
     * `entries.size > 1`. Returns [RoutingResult.SingleItem] otherwise.
     *
     * Each carousel item in [playlistResult].entries is enqueued as an
     * independent [Task] in [downloader] so DownloaderV2 can manage concurrency
     * (MAX_CONCURRENCY = 3) and state transitions independently per item.
     */
    fun routeFromPlaylist(
        originalUrl: String,
        playlistResult: PlaylistResult,
        preferences: DownloadUtil.DownloadPreferences,
        downloader: DownloaderV2,
        selectedIndices: List<Int>? = null,
    ): RoutingResult {

        // Guard: only route Instagram content.
        val isInstagram = playlistResult.extractorKey == INSTAGRAM_EXTRACTOR_KEY
                || originalUrl.contains(INSTAGRAM_HOST)
        if (!isInstagram) return RoutingResult.SingleItem

        // Guard: only route if it is genuinely a multi-item carousel.
        val entries = playlistResult.entries.orEmpty()
        if (entries.size <= 1) {
            Log.d(TAG, "Instagram single item, falling through: $originalUrl")
            return RoutingResult.SingleItem
        }

        val shortcode = extractShortcodeFromUrl(originalUrl) ?: run {
            return RoutingResult.ParseFailed("Could not extract shortcode from: $originalUrl")
        }

        Log.i(TAG, "Instagram carousel: ${entries.size} items, shortcode=$shortcode")

        // Build one Task per carousel entry, with a rich ViewState for the UI.
        val tasks = mutableListOf<Task>()
        val totalItems = entries.size
        val author = playlistResult.uploader ?: playlistResult.channel ?: "unknown"
        
        Log.i(TAG, "Routing Instagram carousel: shortcode=$shortcode, items=$totalItems, author=$author")
        Log.d(TAG, "selectedIndices in router: $selectedIndices")
        entries.forEachIndexed { index, entry ->
            val entryId = entry.id
            val isVideoItem = (entry.duration ?: 0.0) > 0.0
            
            // Filter: only enqueue if it was selected by the user (using 0-based index)
            val isSelected = selectedIndices == null || index in selectedIndices
            
            if (!isSelected) {
                Log.d(TAG, "Skipping non-selected carousel entry $index: id=${entry.id}")
                return@forEachIndexed
            }
                    // CRITICAL FIX: Always use originalUrl (the valid Instagram post URL) with Playlist(index + 1).
            // Do NOT construct fake /p/subitem_id/ URLs which return 404 / "No video formats found!" errors on Instagram.
            val taskUrl = originalUrl
            val taskType = Task.TypeInfo.Playlist(index + 1)
            val cleanTitle = "IG_Post_${author}_item_${index + 1}"
            val itemPreferences = preferences.copy(newTitle = cleanTitle)

            Log.i(TAG, "[Pipeline] Enqueuing selected carousel task item ${index + 1}/$totalItems: url=$taskUrl")

            val task = Task(
                url = taskUrl, 
                type = taskType,
                preferences = itemPreferences
            )
            
            // Rich ViewState: title shows author + item position
            val viewState = Task.ViewState(
                url = taskUrl,
                title = "(@$author) Item ${index + 1} of $totalItems",
                uploader = author,
                extractorKey = "Instagram",
                duration = entry.duration?.toInt() ?: 0,
                thumbnailUrl = entry.thumbnails?.lastOrNull()?.url,
            )
            downloader.enqueue(task, Task.State(
                downloadState = Task.DownloadState.Idle,
                videoInfo = null,
                viewState = viewState,
            ))
            tasks.add(task)
            Log.d(TAG, "Enqueued carousel task ${index + 1}/$totalItems: ${task.id}")
        }

        if (tasks.isEmpty()) {
            return RoutingResult.ParseFailed("No items were selected or all were skipped.")
        }

        Log.i(TAG, "Carousel enqueued: ${tasks.size} tasks for $shortcode")
        return RoutingResult.CarouselEnqueued(
            tasks = tasks,
            itemCount = tasks.size,
            shortcode = shortcode,
            author = author,
        )
    }

    // -----------------------------------------------------------------------
    // Entry point 2: Raw JSON path (fallback / unit-test)
    // -----------------------------------------------------------------------

    /**
     * Routes based on a raw yt-dlp JSON string.
     * Only used when [PlaylistResult] is not available directly.
     * For JVM unit tests and custom command output.
     */
    fun route(
        url: String,
        rawYtDlpJson: String,
        preferences: DownloadUtil.DownloadPreferences,
        downloader: DownloaderV2,
    ): RoutingResult {
        if (!url.contains(INSTAGRAM_HOST)) return RoutingResult.SingleItem
        if (!InstagramCarouselDetector.isCarousel(rawYtDlpJson)) return RoutingResult.SingleItem

        val shortcode = extractShortcodeFromUrl(url)
            ?: return RoutingResult.ParseFailed("Could not extract shortcode from: $url")

        val itemJsonList = InstagramCarouselDetector.extractItemJsonList(rawYtDlpJson)
        if (itemJsonList.isEmpty()) {
            return RoutingResult.ParseFailed("Carousel detected but item list is empty.")
        }

        val tasks = itemJsonList.mapIndexedNotNull { index, itemJson ->
            val itemUrl = extractUrlFromItemJson(itemJson) ?: run {
                Log.w(TAG, "Skipping item $index — no URL found in JSON")
                return@mapIndexedNotNull null
            }
            val task = Task(url = itemUrl, preferences = preferences)
            downloader.enqueue(task)
            task
        }

        if (tasks.isEmpty()) return RoutingResult.ParseFailed("No usable item URLs found.")

        val author = extractAuthor(rawYtDlpJson)
        return RoutingResult.CarouselEnqueued(
            tasks = tasks,
            itemCount = tasks.size,
            shortcode = shortcode,
            author = author,
        )
    }

    // -----------------------------------------------------------------------
    // Pure helpers — regex only, no Android dependencies
    // -----------------------------------------------------------------------

    /** Extracts Instagram shortcode from /p/, /reel/, /reels/, or /tv/ URLs. */
    fun extractShortcodeFromUrl(url: String): String? =
        Regex("""instagram\.com/(?:p|reel|reels|tv)/([A-Za-z0-9_\-]+)""")
            .find(url)?.groupValues?.get(1)

    private fun extractUrlFromItemJson(json: String): String? =
        Regex(""""url"\s*:\s*"([^"]+)"""")
            .find(json)?.groupValues?.get(1)

    private fun extractAuthor(json: String): String =
        Regex(""""uploader"\s*:\s*"([^"]*?)"""")
            .find(json)?.groupValues?.get(1) ?: "unknown"
}
