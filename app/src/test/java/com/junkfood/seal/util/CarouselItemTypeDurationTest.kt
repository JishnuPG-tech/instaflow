package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarouselItemTypeDurationTest {

    @Test
    fun testCarouselItemTypeDerivedFromDuration() {
        val entries = listOf(
            PlaylistEntry(id = "slide_1", url = "https://instagram.fcc.net/image1.jpg", duration = null),
            PlaylistEntry(id = "slide_2", url = "https://instagram.fcc.net/video2.mp4", duration = 14.5),
            PlaylistEntry(id = "slide_3", url = "https://instagram.fcc.net/image3.jpg", duration = 0.0),
        )

        val items = entries.mapIndexed { index, entry ->
            val isVideoEntry = (entry.duration ?: 0.0) > 0.0
            InstagramMediaItem(
                id = entry.id ?: "slide_$index",
                shortcode = "C-0XgP_x-9j",
                mediaType = if (isVideoEntry) InstagramMediaType.VIDEO else InstagramMediaType.IMAGE,
                downloadUrl = entry.url ?: "",
                thumbnailUrl = entry.url ?: "",
                authorUsername = "test_user",
                isVideo = isVideoEntry,
                durationSeconds = entry.duration?.toInt() ?: 0,
                carouselIndex = index,
                totalCarouselItems = entries.size,
            )
        }

        assertEquals(3, items.size)

        // Slide 1: Image
        assertEquals(InstagramMediaType.IMAGE, items[0].mediaType)
        assertFalse(items[0].isVideo)
        assertEquals(0, items[0].durationSeconds)

        // Slide 2: Video
        assertEquals(InstagramMediaType.VIDEO, items[1].mediaType)
        assertTrue(items[1].isVideo)
        assertEquals(14, items[1].durationSeconds)

        // Slide 3: Image
        assertEquals(InstagramMediaType.IMAGE, items[2].mediaType)
        assertFalse(items[2].isVideo)
        assertEquals(0, items[2].durationSeconds)
    }
}
