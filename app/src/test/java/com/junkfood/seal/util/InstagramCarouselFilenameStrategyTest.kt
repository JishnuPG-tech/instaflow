package com.junkfood.seal.util

import com.junkfood.seal.database.InstagramMediaItem
import com.junkfood.seal.database.InstagramMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselFilenameStrategyTest {

    private fun imageItem(index: Int, total: Int) = InstagramMediaItem(
        id = "img_$index", shortcode = "AbCdEfGhIjK",
        mediaType = InstagramMediaType.IMAGE,
        downloadUrl = "https://cdn.instagram.com/img.jpg",
        thumbnailUrl = "", authorUsername = "testuser",
        isVideo = false, carouselIndex = index, totalCarouselItems = total
    )

    private fun videoItem(index: Int, total: Int) = InstagramMediaItem(
        id = "vid_$index", shortcode = "AbCdEfGhIjK",
        mediaType = InstagramMediaType.VIDEO,
        downloadUrl = "https://cdn.instagram.com/vid.mp4",
        thumbnailUrl = "", authorUsername = "testuser",
        isVideo = true, carouselIndex = index, totalCarouselItems = total
    )

    @Test
    fun testImageFilename() {
        val name = InstagramCarouselFilenameStrategy.generateFilename(imageItem(0, 5))
        assertEquals("AbCdEfGhIjK_1_of_5.jpg", name)
    }

    @Test
    fun testVideoFilename() {
        val name = InstagramCarouselFilenameStrategy.generateFilename(videoItem(2, 5))
        assertEquals("AbCdEfGhIjK_3_of_5.mp4", name)
    }

    @Test
    fun testFilenameWithAuthor() {
        val name = InstagramCarouselFilenameStrategy.generateFilenameWithAuthor(imageItem(0, 3))
        assertEquals("testuser_AbCdEfGhIjK_1_of_3.jpg", name)
    }

    @Test
    fun testAuthorWithSpecialCharsIsSanitized() {
        val item = imageItem(0, 2).copy(authorUsername = "test.user@ig")
        val name = InstagramCarouselFilenameStrategy.generateFilenameWithAuthor(item)
        assertTrue(name.startsWith("test_user_ig_"))
    }

    @Test
    fun testDirectoryName() {
        val dir = InstagramCarouselFilenameStrategy.carouselDirectoryName(imageItem(0, 5))
        assertEquals("AbCdEfGhIjK", dir)
    }
}
