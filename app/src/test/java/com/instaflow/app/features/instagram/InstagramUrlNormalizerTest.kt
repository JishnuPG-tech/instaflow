package com.instaflow.app.features.instagram

import com.instaflow.app.features.instagram.url.InstagramUrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

class InstagramUrlNormalizerTest {

    @Test
    fun testStripTrackingParameters() {
        val input = "https://www.instagram.com/p/DFa123/?igsh=MWF5N204YmEx&utm_source=copy_link&fbclid=123"
        val expected = "https://www.instagram.com/p/DFa123/"
        val actual = InstagramUrlNormalizer.normalize(input)
        assertEquals(expected, actual)
    }

    @Test
    fun testCleanUrlPreserved() {
        val input = "https://www.instagram.com/reel/C123456789/"
        val actual = InstagramUrlNormalizer.normalize(input)
        assertEquals(input, actual)
    }
}
