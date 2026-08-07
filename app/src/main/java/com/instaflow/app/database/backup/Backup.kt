package com.instaflow.app.database.backup

import com.instaflow.app.database.objects.CommandTemplate
import com.instaflow.app.database.objects.DownloadedVideoInfo
import com.instaflow.app.database.objects.OptionShortcut
import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val templates: List<CommandTemplate>? = null,
    val shortcuts: List<OptionShortcut>? = null,
    val downloadHistory: List<DownloadedVideoInfo>? = null,
)
