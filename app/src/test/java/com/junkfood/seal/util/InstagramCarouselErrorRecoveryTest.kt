package com.junkfood.seal.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramCarouselErrorRecoveryTest {

    @Test
    fun testClassifyNetworkTimeout() {
        assertEquals(
            InstagramCarouselErrorRecovery.FailureType.NETWORK_TIMEOUT,
            InstagramCarouselErrorRecovery.classifyError("Connection timed out after 30s")
        )
    }

    @Test
    fun testClassify403Forbidden() {
        assertEquals(
            InstagramCarouselErrorRecovery.FailureType.HTTP_403_FORBIDDEN,
            InstagramCarouselErrorRecovery.classifyError("HTTP 403 Forbidden")
        )
    }

    @Test
    fun testClassify429RateLimited() {
        assertEquals(
            InstagramCarouselErrorRecovery.FailureType.HTTP_429_RATE_LIMITED,
            InstagramCarouselErrorRecovery.classifyError("HTTP 429 Too Many Requests")
        )
    }

    @Test
    fun testClassifyParseError() {
        assertEquals(
            InstagramCarouselErrorRecovery.FailureType.PARSE_ERROR,
            InstagramCarouselErrorRecovery.classifyError("Malformed JSON response")
        )
    }

    @Test
    fun testClassifyDiskFull() {
        assertEquals(
            InstagramCarouselErrorRecovery.FailureType.DISK_FULL,
            InstagramCarouselErrorRecovery.classifyError("No space left on device (ENOSPC)")
        )
    }

    @Test
    fun testRecoveryForTimeout() {
        val action = InstagramCarouselErrorRecovery.recommendRecovery(
            InstagramCarouselErrorRecovery.FailureType.NETWORK_TIMEOUT
        )
        assertTrue(action.shouldRetry)
        assertEquals(3, action.maxRetries)
        assertEquals(3000L, action.retryDelayMs)
    }

    @Test
    fun testNoRetryFor403() {
        val action = InstagramCarouselErrorRecovery.recommendRecovery(
            InstagramCarouselErrorRecovery.FailureType.HTTP_403_FORBIDDEN
        )
        assertFalse(action.shouldRetry)
        assertEquals(0, action.maxRetries)
    }

    @Test
    fun testRateLimitHasLongDelay() {
        val action = InstagramCarouselErrorRecovery.recommendRecovery(
            InstagramCarouselErrorRecovery.FailureType.HTTP_429_RATE_LIMITED
        )
        assertTrue(action.retryDelayMs >= 30000L)
    }
}
