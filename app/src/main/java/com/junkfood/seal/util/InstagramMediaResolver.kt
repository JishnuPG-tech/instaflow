package com.junkfood.seal.util

import com.junkfood.seal.util.InstagramUrlParseResult

data class InstagramResolveOptions(
    val cookiesFilePath: String? = null,
    val userAgent: String = DEFAULT_USER_AGENT,
    val includeMetadata: Boolean = true
) {
    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1"
    }
}

object InstagramMediaResolver {

    fun buildYtDlpArgs(
        parseResult: InstagramUrlParseResult,
        options: InstagramResolveOptions = InstagramResolveOptions()
    ): List<String> {
        require(parseResult.isValid) { "Cannot resolve invalid Instagram URL: ${parseResult.rawUrl}" }

        val args = mutableListOf<String>()

        // Force JSON output dump for extraction
        args.add("--dump-json")
        args.add("--no-warnings")
        args.add("--no-call-home")

        // Add custom browser User-Agent
        args.add("--add-header")
        args.add("User-Agent:${options.userAgent}")

        // Add cookies file if provided
        val cookies = options.cookiesFilePath
        if (!cookies.isNullOrEmpty()) {
            args.add("--cookies")
            args.add(cookies)
        }

        // Add target URL
        args.add(parseResult.rawUrl)

        return args
    }
}
