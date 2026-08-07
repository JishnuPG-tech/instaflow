package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InstagramProfilePicHandlerTest {

    @Test
    fun testParseProfilePicJson() {
        val sampleJson = """
            {
                "id": "user_12345678",
                "url": "https://instagram.fna.fbcdn.net/v/profile_pic_hd.jpg",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/profile_pic_hd.jpg"
            }
        """.trimIndent()

        val item = InstagramProfilePicHandler.parseProfilePicJson(sampleJson, "therock")

        assertEquals("user_12345678", item.id)
        assertEquals("therock", item.shortcode)
        assertEquals("therock", item.authorUsername)
        assertEquals(InstagramMediaType.PROFILE_PIC, item.mediaType)
        assertFalse(item.isVideo)
        assertEquals("https://instagram.fna.fbcdn.net/v/profile_pic_hd.jpg", item.downloadUrl)
    }
}
