package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramReelPostHandlerTest {

    @Test
    fun testParseReelPostJson() {
        val sampleJson = """
            {
                "id": "reel_11223344",
                "url": "https://instagram.fna.fbcdn.net/v/t50.2886-16/reel.mp4",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/t51.2885-15/reel_thumb.jpg",
                "uploader": "cristiano",
                "title": "Training day",
                "width": 1080,
                "height": 1920,
                "duration": 30
            }
        """.trimIndent()

        val item = InstagramReelPostHandler.parseReelPostJson(sampleJson, "CrNnKkMmOoPp")

        assertEquals("reel_11223344", item.id)
        assertEquals("CrNnKkMmOoPp", item.shortcode)
        assertEquals(InstagramMediaType.REEL, item.mediaType)
        assertTrue(item.isVideo)
        assertEquals("cristiano", item.authorUsername)
        assertEquals("Training day", item.caption)
        assertEquals(30, item.durationSeconds)
    }
}
