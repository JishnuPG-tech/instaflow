package com.instasave.app.core.network.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaFormat(
    @SerialName("formatId") val formatId: String,
    @SerialName("label") val label: String,
    @SerialName("ext") val ext: String,
    @SerialName("url") val url: String? = null,
    @SerialName("vcodec") val vcodec: String? = null,
    @SerialName("acodec") val acodec: String? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("width") val width: Int? = null,
    @SerialName("tbr") val tbr: Double? = null,
    @SerialName("filesizeBytes") val filesizeBytes: Long? = null
)
