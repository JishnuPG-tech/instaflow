package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramVideoPostHandlerTest {

    @Test
    fun testParseVideoPostJson() {
        val sampleJson = """
            {
                "id": "video_98765",
                "url": "https://instagram.fna.fbcdn.net/v/t50.2886-16/video.mp4",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/t51.2885-15/video_thumb.jpg",
                "uploader": "leomessi",
                "title": "Match Highlights",
                "width": 1080,
                "height": 1920
            }
        """.trimIndent()

        val item = InstagramVideoPostHandler.parseVideoPostJson(sampleJson, "Cv987654321")

        assertEquals("video_98765", item.id)
        assertEquals("Cv987654321", item.shortcode)
        assertEquals(InstagramMediaType.VIDEO, item.mediaType)
        assertTrue(item.isVideo)
        assertEquals("leomessi", item.authorUsername)
        assertEquals("Match Highlights", item.caption)
    }
}
