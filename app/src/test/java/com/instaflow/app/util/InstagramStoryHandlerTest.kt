package com.instaflow.app.util

import com.instaflow.app.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramStoryHandlerTest {

    @Test
    fun testParsePhotoStoryJson() {
        val sampleJson = """
            {
                "id": "story_photo_001",
                "url": "https://instagram.fna.fbcdn.net/v/story_photo.jpg",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/story_thumb.jpg",
                "uploader": "neymarjr",
                "is_video": false,
                "width": 1080,
                "height": 1920
            }
        """.trimIndent()

        val item = InstagramStoryHandler.parseStoryJson(sampleJson, "story_photo_001")

        assertEquals("story_photo_001", item.id)
        assertEquals(InstagramMediaType.STORY, item.mediaType)
        assertFalse(item.isVideo)
        assertEquals("neymarjr", item.authorUsername)
    }

    @Test
    fun testParseVideoStoryJson() {
        val sampleJson = """
            {
                "id": "story_video_002",
                "url": "https://instagram.fna.fbcdn.net/v/story_video.mp4",
                "thumbnail": "https://instagram.fna.fbcdn.net/v/story_thumb2.jpg",
                "uploader": "neymarjr",
                "is_video": true,
                "width": 1080,
                "height": 1920,
                "duration": 15
            }
        """.trimIndent()

        val item = InstagramStoryHandler.parseStoryJson(sampleJson, "story_video_002")

        assertEquals("story_video_002", item.id)
        assertEquals(InstagramMediaType.STORY, item.mediaType)
        assertTrue(item.isVideo)
        assertEquals(15, item.durationSeconds)
    }
}
