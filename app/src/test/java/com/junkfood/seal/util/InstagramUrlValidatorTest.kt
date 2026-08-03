package com.junkfood.seal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramUrlValidatorTest {

    @Test
    fun testReelUrlParsing() {
        val url = "https://www.instagram.com/reel/C1xAbCdEfGh/?igsh=MzRlODBiNWFlZA=="
        val result = InstagramUrlValidator.parseUrl(url)

        assertTrue(result.isValid)
        assertEquals(InstagramUrlType.REEL, result.type)
        assertEquals("C1xAbCdEfGh", result.shortcode)
    }

    @Test
    fun testPostUrlParsing() {
        val url = "https://instagram.com/p/Cz123456789/"
        val result = InstagramUrlValidator.parseUrl(url)

        assertTrue(result.isValid)
        assertEquals(InstagramUrlType.POST, result.type)
        assertEquals("Cz123456789", result.shortcode)
    }

    @Test
    fun testStoryUrlParsing() {
        val url = "https://www.instagram.com/stories/test_user/3216549870123/"
        val result = InstagramUrlValidator.parseUrl(url)

        assertTrue(result.isValid)
        assertEquals(InstagramUrlType.STORY, result.type)
        assertEquals("test_user", result.username)
        assertEquals("3216549870123", result.shortcode)
    }

    @Test
    fun testProfileUrlParsing() {
        val url = "https://www.instagram.com/cristiano/"
        val result = InstagramUrlValidator.parseUrl(url)

        assertTrue(result.isValid)
        assertEquals(InstagramUrlType.PROFILE, result.type)
        assertEquals("cristiano", result.username)
    }

    @Test
    fun testInvalidUrlParsing() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val result = InstagramUrlValidator.parseUrl(url)

        assertFalse(result.isValid)
        assertEquals(InstagramUrlType.UNKNOWN, result.type)
    }
}
