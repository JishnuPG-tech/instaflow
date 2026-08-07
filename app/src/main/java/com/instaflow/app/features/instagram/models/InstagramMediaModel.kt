package com.instaflow.app.features.instagram.models

data class InstagramMediaModel(
    val id: String,
    val shortcode: String,
    val type: InstagramMediaType,
    val ownerUsername: String,
    val ownerFullName: String? = null,
    val caption: String? = null,
    val thumbnailUrl: String,
    val durationSeconds: Int = 0,
    val isPrivate: Boolean = false,
    val requiresAccount: Boolean = false,
    val estimatedDownloadSize: Long = 0L,
    val metadataLoaded: Boolean = false,
    val availableVideoTracks: List<InstagramFormat> = emptyList(),
    val availableAudioTracks: List<InstagramFormat> = emptyList(),
    val availableImageVariants: List<InstagramFormat> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
