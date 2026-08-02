package com.instasave.app.core.network.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CarouselItem(
    @SerialName("index") val index: Int,
    @SerialName("type") val type: String, // photo | video
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerialName("formats") val formats: List<MediaFormat>
)
