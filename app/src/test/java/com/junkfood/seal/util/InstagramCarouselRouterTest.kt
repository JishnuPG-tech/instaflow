package com.junkfood.seal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WP 4.1 — InstagramCarouselRouter Unit Tests
 *
 * Only the pure-function surface of the Router is testable in JVM unit tests:
 * - extractShortcodeFromUrl()
 * - InstagramCarouselDetector.extractItemJsonList()
 *
 * The route() method itself requires DownloaderV2 (Compose/Koin/Android) and
 * is covered by the Phase 4 integration/manual verification checklist.
 */
class InstagramCarouselRouterTest {

    // -----------------------------------------------------------------------
    // extractShortcodeFromUrl
    // -----------------------------------------------------------------------

    @Test
    fun testExtractShortcodeFromPostUrl() {
        val url = "https://www.instagram.com/p/AbCdEfGhIjK/"
        assertEquals("AbCdEfGhIjK", InstagramCarouselRouter.extractShortcodeFromUrl(url))
    }

    @Test
    fun testExtractShortcodeFromReelUrl() {
        val url = "https://www.instagram.com/reel/XyZ123456789/"
        assertEquals("XyZ123456789", InstagramCarouselRouter.extractShortcodeFromUrl(url))
    }

    @Test
    fun testExtractShortcodeFromTvUrl() {
        val url = "https://www.instagram.com/tv/TeStIdXxXx/?igsh=abc"
        assertEquals("TeStIdXxXx", InstagramCarouselRouter.extractShortcodeFromUrl(url))
    }

    @Test
    fun testExtractShortcodeFromNonInstagramUrl() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        assertNull(InstagramCarouselRouter.extractShortcodeFromUrl(url))
    }

    @Test
    fun testExtractShortcodeWithTrailingQueryParams() {
        val url = "https://www.instagram.com/p/CqLmN8HoP12/?igsh=dummyshare"
        assertEquals("CqLmN8HoP12", InstagramCarouselRouter.extractShortcodeFromUrl(url))
    }

    // -----------------------------------------------------------------------
    // extractItemJsonList (method added to InstagramCarouselDetector)
    // -----------------------------------------------------------------------

    @Test
    fun testExtractItemJsonListTwoItems() {
        val json = """{"__typename":"GraphSidecar","playlist_count":2,"entries":[{"id":"item1","url":"https://cdn.ig.com/a.jpg","is_video":false},{"id":"item2","url":"https://cdn.ig.com/b.mp4","is_video":true,"duration":5}]}"""
        val items = InstagramCarouselDetector.extractItemJsonList(json)
        assertEquals(2, items.size)
        assertTrue(items[0].contains("item1"))
        assertTrue(items[1].contains("item2"))
    }

    @Test
    fun testExtractItemJsonListThreeItems() {
        val json = """{"__typename":"GraphSidecar","entries":[{"id":"a"},{"id":"b"},{"id":"c"}]}"""
        val items = InstagramCarouselDetector.extractItemJsonList(json)
        assertEquals(3, items.size)
    }

    @Test
    fun testExtractItemJsonListReturnsEmptyForNonCarousel() {
        val json = """{"__typename":"GraphImage","id":"abc123"}"""
        val items = InstagramCarouselDetector.extractItemJsonList(json)
        assertTrue(items.isEmpty())
    }

    @Test
    fun testExtractItemJsonListReturnsEmptyWhenEntriesEmpty() {
        val json = """{"__typename":"GraphSidecar","entries":[]}"""
        val items = InstagramCarouselDetector.extractItemJsonList(json)
        assertTrue(items.isEmpty())
    }
}
