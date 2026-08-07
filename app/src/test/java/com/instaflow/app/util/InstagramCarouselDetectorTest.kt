package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselDetectorTest {

    @Test
    fun testDetectsCarouselByTypename() {
        val json = """{"__typename":"GraphSidecar","id":"abc123"}"""
        assertTrue(InstagramCarouselDetector.isCarousel(json))
    }

    @Test
    fun testDetectsCarouselBySidecarField() {
        val json = """{"id":"abc123","edge_sidecar_to_children":{"edges":[]}}"""
        assertTrue(InstagramCarouselDetector.isCarousel(json))
    }

    @Test
    fun testDetectsCarouselByPlaylistCount() {
        val json = """{"id":"abc123","playlist_count":5}"""
        assertTrue(InstagramCarouselDetector.isCarousel(json))
        assertEquals(5, InstagramCarouselDetector.detectCarouselCount(json))
    }

    @Test
    fun testNonCarouselSinglePost() {
        val json = """{"id":"abc123","__typename":"GraphImage","playlist_count":1}"""
        assertFalse(InstagramCarouselDetector.isCarousel(json))
        assertEquals(1, InstagramCarouselDetector.detectCarouselCount(json))
    }

    @Test
    fun testInferCarouselItemTypeImage() {
        assertEquals(InstagramMediaType.IMAGE, InstagramCarouselDetector.inferCarouselItemType(false))
    }

    @Test
    fun testInferCarouselItemTypeVideo() {
        assertEquals(InstagramMediaType.VIDEO, InstagramCarouselDetector.inferCarouselItemType(true))
    }
}
