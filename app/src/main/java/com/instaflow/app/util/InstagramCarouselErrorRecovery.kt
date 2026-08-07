package com.instaflow.app.util

/**
 * WP 3.8 — Instagram Carousel Error Recovery
 *
 * Handles failure scenarios in carousel downloads:
 * - Partial failures (some items fail, others succeed)
 * - Retry strategy per item
 * - Recovery recommendations based on failure type
 */
object InstagramCarouselErrorRecovery {

    enum class FailureType {
        NETWORK_TIMEOUT,
        HTTP_403_FORBIDDEN,
        HTTP_429_RATE_LIMITED,
        PARSE_ERROR,
        DISK_FULL,
        UNKNOWN
    }

    data class RecoveryAction(
        val shouldRetry: Boolean,
        val retryDelayMs: Long,
        val maxRetries: Int,
        val userMessage: String
    )

    fun classifyError(throwableMessage: String?): FailureType {
        val msg = throwableMessage?.lowercase() ?: return FailureType.UNKNOWN
        return when {
            "timeout" in msg || "timed out" in msg -> FailureType.NETWORK_TIMEOUT
            "403" in msg || "forbidden" in msg -> FailureType.HTTP_403_FORBIDDEN
            "429" in msg || "rate limit" in msg || "too many" in msg -> FailureType.HTTP_429_RATE_LIMITED
            "parse" in msg || "json" in msg || "malformed" in msg -> FailureType.PARSE_ERROR
            "no space" in msg || "disk full" in msg || "enospc" in msg -> FailureType.DISK_FULL
            else -> FailureType.UNKNOWN
        }
    }

    fun recommendRecovery(failureType: FailureType): RecoveryAction {
        return when (failureType) {
            FailureType.NETWORK_TIMEOUT -> RecoveryAction(
                shouldRetry = true, retryDelayMs = 3000L, maxRetries = 3,
                userMessage = "Network timeout. Retrying..."
            )
            FailureType.HTTP_403_FORBIDDEN -> RecoveryAction(
                shouldRetry = false, retryDelayMs = 0L, maxRetries = 0,
                userMessage = "Access denied. The content may be private or require login."
            )
            FailureType.HTTP_429_RATE_LIMITED -> RecoveryAction(
                shouldRetry = true, retryDelayMs = 30000L, maxRetries = 2,
                userMessage = "Rate limited by Instagram. Waiting before retry..."
            )
            FailureType.PARSE_ERROR -> RecoveryAction(
                shouldRetry = false, retryDelayMs = 0L, maxRetries = 0,
                userMessage = "Could not parse media metadata. The format may have changed."
            )
            FailureType.DISK_FULL -> RecoveryAction(
                shouldRetry = false, retryDelayMs = 0L, maxRetries = 0,
                userMessage = "Insufficient storage. Please free up space and try again."
            )
            FailureType.UNKNOWN -> RecoveryAction(
                shouldRetry = true, retryDelayMs = 5000L, maxRetries = 1,
                userMessage = "Unknown error. Retrying once..."
            )
        }
    }
}
