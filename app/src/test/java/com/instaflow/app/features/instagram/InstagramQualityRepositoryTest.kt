package com.instaflow.app.features.instagram

import com.instaflow.app.features.instagram.repository.InstagramQualityRepository
import com.instaflow.app.util.Format
import com.instaflow.app.util.VideoInfo
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
        
        // First option should be Optimal Quality (Auto)
        assertEquals("Optimal Quality (Auto)", uiModel.videoQualityOptions.first().resolutionLabel)
        
        // Second option should be High Definition (1080p) and have a size
        val hdFormat = uiModel.videoQualityOptions.find { it.resolutionLabel.contains("1080p") }
        assertTrue(hdFormat != null)
        assertTrue(hdFormat?.formattedSize?.contains("MB") == true)

        // Ensure raw format IDs and "unknown" cards are NEVER exposed
        assertFalse(uiModel.videoQualityOptions.any { it.resolutionLabel.contains("dash") })
        assertFalse(uiModel.videoQualityOptions.any { it.resolutionLabel.contains("unknown") })
    }

    @Test
    fun testPhotoWithMusicDetection() {
        val formats = listOf(
            Format(formatId = "0", vcodec = "none", acodec = "none", width = 1080.0, height = 1080.0),
            Format(formatId = "audio", vcodec = "none", acodec = "mp4a", abr = 128.0)
        )
        val info = VideoInfo(
            id = "photo123",
            title = "Photo with music",
            uploader = "artist",
            duration = 15.0,
            formats = formats
        )
        
        val uiModel = InstagramQualityRepository.mapToUiModel(info)
        assertEquals("Photo with Music", uiModel.mediaTypeLabel)
        assertTrue(uiModel.videoQualityOptions.any { it.resolutionLabel == "Original HD" })
        assertTrue(uiModel.audioQualityOptions.any { it.resolutionLabel.contains("Background Music") })
    }
}
