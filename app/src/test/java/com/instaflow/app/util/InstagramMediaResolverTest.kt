package com.instaflow.app.util

import com.instaflow.app.util.InstagramMediaResolver
import com.instaflow.app.util.InstagramResolveOptions
import com.instaflow.app.util.InstagramUrlValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramMediaResolverTest {

    @Test
    fun testYtDlpArgsGenerationWithoutCookies() {
        val parseResult = InstagramUrlValidator.parseUrl("https://www.instagram.com/reel/C1xAbCdEfGh/")
        val args = InstagramMediaResolver.buildYtDlpArgs(parseResult)

        assertTrue(args.contains("--dump-json"))
        assertTrue(args.contains("--no-warnings"))
        assertTrue(args.contains("https://www.instagram.com/reel/C1xAbCdEfGh/"))
    }

    @Test
    fun testYtDlpArgsGenerationWithCookies() {
        val parseResult = InstagramUrlValidator.parseUrl("https://www.instagram.com/p/Cz123456789/")
        val options = InstagramResolveOptions(cookiesFilePath = "/sdcard/Download/cookies.txt")
        val args = InstagramMediaResolver.buildYtDlpArgs(parseResult, options)

        assertTrue(args.contains("--cookies"))
        assertTrue(args.contains("/sdcard/Download/cookies.txt"))
    }
}
