package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InstagramImagePostHandlerTest {

    @Test
    fun testParseImagePostJson() {
        val sampleJson = """
            {
                "id": "321654987",
                "url": "https://instagram.fna.fbcdn.net/v/t51.2885-15/img.jpg",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/t51.2885-15/thumb.jpg",
                "uploader": "test_user",
                "title": "Beautiful Sunset",
                "width": 1080,
                "height": 1350
            }
        """.trimIndent()

        val item = InstagramImagePostHandler.parseImagePostJson(sampleJson, "Cz123456789")

        assertEquals("321654987", item.id)
        assertEquals("Cz123456789", item.shortcode)
        assertEquals(InstagramMediaType.IMAGE, item.mediaType)
        assertFalse(item.isVideo)
        assertEquals("test_user", item.authorUsername)
        assertEquals("Beautiful Sunset", item.caption)
    }
}
