package com.junkfood.seal.features.instagram

import com.junkfood.seal.features.instagram.repository.InstagramQualityRepository
import com.junkfood.seal.util.Format
import com.junkfood.seal.util.VideoInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstagramQualityRepositoryTest {

    @Test
    fun testFormatMappingHidesDashAndUnknown() {
        val dummyFormats = listOf(
            Format(formatId = "dash-1230206290181168v", vcodec = "vp09", acodec = "none", width = 1080.0, height = 810.0, fileSize = 18000000.0),
            Format(formatId = "dash-1230155340186263a", vcodec = "none", acodec = "mp4a", fileSize = 3000000.0),
            Format(formatId = "3", vcodec = "unknown", acodec = "unknown")
        )

        val info = VideoInfo(
            id = "DFa123",
            title = "Test Reel Caption",
            uploader = "mystery_motion.11",
            duration = 35.0,
            thumbnail = "https://instagram.com/thumb.jpg",
            formats = dummyFormats
        )

        val uiModel = InstagramQualityRepository.mapToUiModel(info)

        assertEquals("@mystery_motion.11", uiModel.authorHandle)
        assertEquals("Test Reel Caption", uiModel.caption)
        assertEquals("00:35", uiModel.durationFormatted)
        assertTrue(uiModel.videoQualityOptions.isNotEmpty())
        assertEquals("Original (1080p)", uiModel.videoQualityOptions.first().resolutionLabel)
        assertTrue(uiModel.videoQualityOptions.first().formattedSize.contains("MB"))

        // Ensure raw format IDs and "unknown" cards are NEVER exposed
        assertFalse(uiModel.videoQualityOptions.any { it.resolutionLabel.contains("dash") })
        assertFalse(uiModel.videoQualityOptions.any { it.resolutionLabel.contains("unknown") })
    }
}
