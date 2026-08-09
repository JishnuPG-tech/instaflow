package com.instaflow.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ContentType {
    REEL,
    VIDEO_POST,
    IMAGE_POST,
    CAROUSEL,
    STORY_IMAGE,
    STORY_VIDEO,
    PROFILE_PHOTO,
    OTHER
}

@Serializable
enum class MediaItemType {
    IMAGE,
    VIDEO
}

@Serializable
data class MediaCapabilities(
    val canDownloadImage: Boolean = false,
    val canDownloadVideo: Boolean = false,
    val canDownloadAudio: Boolean = false,
    val canExtractAudio: Boolean = false,
    val canMuxAudioWithImage: Boolean = false,
    val canMuxAudioWithVideo: Boolean = false,
    val canDownloadCarousel: Boolean = false,
    val canSelectItems: Boolean = false,
    val hasMultipleItems: Boolean = false
)

@Serializable
data class MediaItem(
    val itemId: String,
    val index: Int,
    val type: MediaItemType,
    val thumbnail: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Float = 0.0f,
    val hasVideo: Boolean = false,
    val hasAudio: Boolean = false,
    val imageUrl: String? = null,
    val capabilities: MediaCapabilities? = null
)

@Serializable
data class MediaResult(
    val contentType: ContentType,
    val sourceUrl: String,
    val canonicalUrl: String? = null,
    val id: String,
    val title: String,
    val description: String? = null,
    val author: String,
    val authorUsername: String? = null,
    val authorAvatar: String? = null,
    val thumbnail: String? = null,
    val duration: Float = 0.0f,
    val itemCount: Int = 1,
    val mediaItems: List<MediaItem> = emptyList(),
    val audioAvailable: Boolean = false,
    val capabilities: MediaCapabilities = MediaCapabilities()
)
