package com.instaflow.app.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class InstagramMediaModelTest {

    @Test
    fun testInstagramMediaItemCreation() {
        val item = InstagramMediaItem(
            id = "media_123",
            shortcode = "Cz123456789",
            mediaType = InstagramMediaType.IMAGE,
            downloadUrl = "https://instagram.fsan1-1.fna.fbcdn.net/v/t51.2885-15/e35/123.jpg",
            thumbnailUrl = "https://instagram.fsan1-1.fna.fbcdn.net/v/t51.2885-15/e35/123_thumb.jpg",
            authorUsername = "cristiano",
            caption = "Match day!",
            width = 1080,
            height = 1350,
            isVideo = false
        )

        assertEquals("media_123", item.id)
        assertEquals("Cz123456789", item.shortcode)
        assertEquals(InstagramMediaType.IMAGE, item.mediaType)
        assertFalse(item.isVideo)
        assertEquals(1080, item.width)
        assertEquals("cristiano", item.authorUsername)
    }
}
