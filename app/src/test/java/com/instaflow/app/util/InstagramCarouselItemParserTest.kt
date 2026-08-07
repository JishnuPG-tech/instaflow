package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselItemParserTest {

    @Test
    fun testParseImageCarouselItem() {
        val json = """
            {
                "id": "carousel_item_001",
                "url": "https://instagram.fna.fbcdn.net/v/carousel_img.jpg",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/carousel_thumb.jpg",
                "uploader": "bbcnews",
                "is_video": false,
                "width": 1080,
                "height": 1080
            }
        """.trimIndent()

        val item = InstagramCarouselItemParser.parseCarouselItem(json, "CaRoUsElShOrT", 0, 5)

        assertEquals("carousel_item_001", item.id)
        assertEquals("CaRoUsElShOrT", item.shortcode)
        assertEquals(InstagramMediaType.IMAGE, item.mediaType)
        assertFalse(item.isVideo)
        assertEquals(0, item.carouselIndex)
        assertEquals(5, item.totalCarouselItems)
    }

    @Test
    fun testParseVideoCarouselItem() {
        val json = """
            {
                "id": "carousel_item_002",
                "url": "https://instagram.fna.fbcdn.net/v/carousel_video.mp4",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/carousel_video_thumb.jpg",
                "uploader": "bbcnews",
                "is_video": true,
                "width": 1080,
                "height": 1920,
                "duration": 12
            }
        """.trimIndent()

        val item = InstagramCarouselItemParser.parseCarouselItem(json, "CaRoUsElShOrT", 2, 5)

        assertEquals(InstagramMediaType.VIDEO, item.mediaType)
        assertTrue(item.isVideo)
        assertEquals(12, item.durationSeconds)
        assertEquals(2, item.carouselIndex)
        assertEquals(5, item.totalCarouselItems)
    }

    @Test
    fun testFallbackIdWhenMissing() {
        val json = """{"url":"https://instagram.fna.fbcdn.net/v/img.jpg","is_video":false}"""
        val item = InstagramCarouselItemParser.parseCarouselItem(json, "ShortCodeXYZ", 1, 3)
        assertEquals("ShortCodeXYZ_1", item.id)
    }
}
