package com.instasave.app.core.network.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("status") val status: String,
    @SerialName("ytDlpVersion") val ytDlpVersion: String
)
