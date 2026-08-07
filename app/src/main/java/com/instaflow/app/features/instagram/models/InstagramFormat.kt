package com.instaflow.app.features.instagram.models

data class InstagramFormat(
    val formatId: String,
    val resolutionLabel: String,
    val width: Int = 0,
    val height: Int = 0,
    val ext: String = "mp4",
    val fileSizeApprox: Long = 0L,
    val isAudioOnly: Boolean = false
) {
    val formattedSize: String
        get() {
            if (fileSizeApprox <= 0L) return ""
            val mb = fileSizeApprox / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.2f MB", mb)
        }
}
