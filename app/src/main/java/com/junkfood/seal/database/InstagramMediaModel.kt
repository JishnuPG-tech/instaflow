package com.junkfood.seal.database

enum class InstagramMediaType {
    IMAGE,
    VIDEO,
    REEL,
    STORY,
    PROFILE_PIC,
    CAROUSEL
}

data class InstagramMediaItem(
    val id: String,
    val shortcode: String,
    val mediaType: InstagramMediaType,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val authorUsername: String,
    val authorFullName: String? = null,
    val caption: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val isVideo: Boolean = false,
    val durationSeconds: Int = 0,
    val carouselIndex: Int = 0,
    val totalCarouselItems: Int = 1
)
