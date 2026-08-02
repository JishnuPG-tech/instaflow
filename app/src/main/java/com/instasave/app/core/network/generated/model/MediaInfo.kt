package com.instasave.app.core.network.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String, // post | reel | carousel
    @SerialName("author") val author: String? = null,
    @SerialName("authorDisplayName") val authorDisplayName: String? = null,
    @SerialName("authorAvatarUrl") val authorAvatarUrl: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("caption") val caption: String? = null,
    @SerialName("uploadedAt") val uploadedAt: String? = null,
    @SerialName("durationSeconds") val durationSeconds: Double? = null,
    @SerialName("formats") val formats: List<MediaFormat>? = null,
    @SerialName("items") val items: List<CarouselItem>? = null
)
