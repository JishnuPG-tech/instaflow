package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselOrchestratorTest {

    private val item1Json = """{"id":"item_001","url":"https://cdn.instagram.com/img1.jpg","thumbnail":"https://cdn.instagram.com/thumb1.jpg","uploader":"username_a","is_video":false,"width":1080,"height":1080}"""
    private val item2Json = """{"id":"item_002","url":"https://cdn.instagram.com/video2.mp4","thumbnail":"https://cdn.instagram.com/thumb2.jpg","uploader":"username_a","is_video":true,"width":1080,"height":1920,"duration":8}"""
    private val item3Json = """{"id":"item_003","url":"https://cdn.instagram.com/img3.jpg","thumbnail":"https://cdn.instagram.com/thumb3.jpg","uploader":"username_a","is_video":false,"width":1080,"height":1080}"""

    @Test
    fun testOrchestrateThreeItemCarousel() {
        val items = InstagramCarouselOrchestrator.orchestrate(
            parentShortcode = "CaRoUsEl123",
            itemJsonList = listOf(item1Json, item2Json, item3Json),
            parentCaption = "Check out our trip!",
            authorUsername = "username_a"
        )

        assertEquals(3, items.size)

        // Item 0 — image
        assertEquals("item_001", items[0].id)
        assertEquals(InstagramMediaType.IMAGE, items[0].mediaType)
        assertFalse(items[0].isVideo)
        assertEquals(0, items[0].carouselIndex)
        assertEquals(3, items[0].totalCarouselItems)
        assertEquals("Check out our trip!", items[0].caption)

        // Item 1 — video
        assertEquals("item_002", items[1].id)
        assertEquals(InstagramMediaType.VIDEO, items[1].mediaType)
        assertTrue(items[1].isVideo)
        assertEquals(8, items[1].durationSeconds)
        assertEquals(1, items[1].carouselIndex)

        // Item 2 — image
        assertEquals("item_003", items[2].id)
        assertEquals(2, items[2].carouselIndex)
    }

    @Test
    fun testShouldUseCarouselPipeline() {
        val carouselJson = """{"__typename":"GraphSidecar"}"""
        val singleJson = """{"__typename":"GraphImage"}"""
        assertTrue(InstagramCarouselOrchestrator.shouldUseCarouselPipeline(carouselJson))
        assertFalse(InstagramCarouselOrchestrator.shouldUseCarouselPipeline(singleJson))
    }
}
