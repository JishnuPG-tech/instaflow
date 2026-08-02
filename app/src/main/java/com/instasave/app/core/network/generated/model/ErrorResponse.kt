package com.instasave.app.core.network.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorDetail(
    @SerialName("code") val code: String,
    @SerialName("message") val message: String
)

@Serializable
data class ErrorResponse(
    @SerialName("error") val error: ErrorDetail
)
