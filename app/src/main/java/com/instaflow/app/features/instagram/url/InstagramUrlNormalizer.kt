package com.instaflow.app.features.instagram.url

object InstagramUrlNormalizer {

    /**
     * Cleans an Instagram URL by stripping tracking parameters (?igsh=, ?utm_source=, fbclid=, etc.)
     */
    fun normalize(rawUrl: String): String {
        if (rawUrl.isBlank()) return ""
        val trimmed = rawUrl.trim()
        val questionMarkIndex = trimmed.indexOf('?')
        if (questionMarkIndex == -1) return trimmed

        val baseUrl = trimmed.substring(0, questionMarkIndex)
        val queryString = trimmed.substring(questionMarkIndex + 1)

        val cleanParams = queryString.split('&').filter { param ->
            val key = param.substringBefore('=').lowercase()
            !key.startsWith("utm_") &&
                key != "igsh" &&
                key != "igshid" &&
                key != "fbclid" &&
                key != "share_id"
        }

        return if (cleanParams.isEmpty()) {
            baseUrl
        } else {
            "$baseUrl?${cleanParams.joinToString("&")}"
        }
    }
}
