package com.junkfood.seal.features.instagram.models

data class InstagramUiModel(
    val mediaId: String,
    val authorHandle: String,
    val caption: String,
    val thumbnailUrl: String,
    val durationFormatted: String,
    val mediaTypeLabel: String,
    val videoQualityOptions: List<InstagramFormat> = emptyList(),
    val audioQualityOptions: List<InstagramFormat> = emptyList(),
    val hasAudioOption: Boolean = false
)
