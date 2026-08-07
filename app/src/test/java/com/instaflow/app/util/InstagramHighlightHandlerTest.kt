package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramHighlightHandlerTest {

    @Test
    fun testParseHighlightPhotoItem() {
        val sampleJson = """
            {
                "id": "highlight_photo_abc123",
                "url": "https://instagram.fna.fbcdn.net/v/highlight_photo.jpg",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/highlight_thumb.jpg",
                "uploader": "natgeo",
                "title": "Wildlife 2024",
                "is_video": false,
                "width": 1080,
                "height": 1920
            }
        """.trimIndent()

        val item = InstagramHighlightHandler.parseHighlightJson(sampleJson, "highlight_17891234")

        assertEquals("highlight_photo_abc123", item.id)
        assertEquals("highlight_17891234", item.shortcode)
        assertEquals(InstagramMediaType.STORY, item.mediaType)
        assertFalse(item.isVideo)
        assertEquals("natgeo", item.authorUsername)
        assertEquals("Wildlife 2024", item.caption)
    }

    @Test
    fun testParseHighlightVideoItem() {
        val sampleJson = """
            {
                "id": "highlight_video_xyz789",
                "url": "https://instagram.fna.fbcdn.net/v/highlight_video.mp4",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/highlight_thumb2.jpg",
                "uploader": "nasa",
                "title": "Mars Mission",
                "is_video": true,
                "width": 1080,
                "height": 1920,
                "duration": 25
            }
        """.trimIndent()

        val item = InstagramHighlightHandler.parseHighlightJson(sampleJson, "highlight_99999999")

        assertEquals(InstagramMediaType.STORY, item.mediaType)
        assertTrue(item.isVideo)
        assertEquals(25, item.durationSeconds)
        assertEquals("nasa", item.authorUsername)
    }
}
