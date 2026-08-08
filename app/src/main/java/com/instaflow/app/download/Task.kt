package com.instaflow.app.download

import com.instaflow.app.database.objects.CommandTemplate
import com.instaflow.app.download.Task.TypeInfo
import com.instaflow.app.download.Task.ViewState
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.Format
import com.instaflow.app.util.VideoInfo
import com.instaflow.app.util.toDurationText
import com.instaflow.app.util.toHttpsUrl
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.roundToInt

private val TypeInfo.id: String
    get() =
        when (this) {
            is TypeInfo.CustomCommand -> "${template.id}_${template.name}"
            is TypeInfo.Playlist -> "$index"
            TypeInfo.URL -> ""
        }

private fun makeId(url: String, type: TypeInfo, preferences: DownloadUtil.DownloadPreferences): String =
    "${url}_${type.id}_${preferences.hashCode()}"

@Serializable
data class Task(
    val url: String,
    val type: TypeInfo = TypeInfo.URL,
    val preferences: DownloadUtil.DownloadPreferences,
    val id: String = makeId(url, type, preferences),
) : Comparable<Task> {

    val timeCreated: Long = System.currentTimeMillis()

    override fun compareTo(other: Task): Int {
        return timeCreated.compareTo(other.timeCreated)
    }

    @Serializable
    sealed interface TypeInfo {

        @Serializable data class Playlist(val index: Int = 0) : TypeInfo

        @Serializable data class CustomCommand(val template: CommandTemplate) : TypeInfo

        @Serializable data object URL : TypeInfo
    }

    @Serializable
    data class State(
        val downloadState: DownloadState,
        val videoInfo: VideoInfo?,
        val viewState: ViewState,
    )

    @Serializable
    sealed interface DownloadState : Comparable<DownloadState> {

        interface Cancelable {
            val job: Job
            val taskId: String
            val action: RestartableAction
        }

        interface Restartable {
            val action: RestartableAction
        }

        @Serializable data object Idle : DownloadState

        @Serializable
        data class FetchingInfo(
            @Transient override val job: Job = Job(),
            override val taskId: String,
        ) : DownloadState, Cancelable {
            override val action: RestartableAction = RestartableAction.FetchInfo
        }

        @Serializable data object ReadyWithInfo : DownloadState

        @Serializable
        data class Running(
            @Transient override val job: Job = Job(),
            override val taskId: String,
            val progress: Float = PROGRESS_INDETERMINATE,
            val progressText: String = "",
        ) : DownloadState, Cancelable {
            override val action: RestartableAction = RestartableAction.Download
        }

        @Serializable
        data class Canceled(override val action: RestartableAction, val progress: Float? = null) :
            DownloadState, Restartable

        @Serializable
        data class Error(
            @Transient val throwable: Throwable = Throwable(),
            override val action: RestartableAction,
        ) : DownloadState, Restartable

        @Serializable data class Completed(val filePath: String?) : DownloadState

        override fun compareTo(other: DownloadState): Int {
            return ordinal - other.ordinal
        }

        private val ordinal: Int
            get() =
                when (this) {
                    is Canceled -> 4
                    is Error -> 5
                    is Completed -> 6
                    Idle -> 3
                    is FetchingInfo -> 2
                    ReadyWithInfo -> 1
                    is Running -> 0
                }
    }

    @Serializable
    sealed interface RestartableAction {
        @Serializable data object FetchInfo : RestartableAction

        @Serializable data object Download : RestartableAction
    }

    @Serializable
    data class ViewState(
        val url: String = "https://www.example.com",
        val title: String = "",
        val uploader: String = "",
        val extractorKey: String = "",
        val duration: Int = 0,
        val durationText: String? = null,
        val fileSizeApprox: Double = .0,
        val thumbnailUrl: String? = null,
        val videoFormats: List<Format>? = null,
        val audioOnlyFormats: List<Format>? = null,
        val mediaType: MediaType = MediaType.VIDEO,
    ) {
        @Serializable
        enum class MediaType {
            VIDEO, PHOTO, CAROUSEL, PROFILE_PIC, UNKNOWN
        }

        companion object {
            fun fromVideoInfo(info: VideoInfo): ViewState {
                val formats =
                    info.requestedFormats
                        ?: info.requestedDownloads?.map { it.toFormat() }
                        ?: emptyList()

                val videoFormats = formats.filter { it.containsVideo() }
                val audioOnlyFormats = formats.filter { it.isAudioOnly() }

                val durationSec = info.duration?.toInt() ?: 0
                val isInstagram = info.extractorKey.lowercase().contains("instagram")
                val hasVideo = info.formats?.any { it.vcodec != "none" && it.vcodec != null } ?: false
                
                val mediaType = when {
                    isInstagram && (info.webpageUrl?.contains("/p/") == true || info.webpageUrl?.contains("/reels/") == true || info.webpageUrl?.contains("/reel/") == true) && hasVideo -> MediaType.VIDEO
                    isInstagram && (info.webpageUrl?.contains("/p/") == true) && !hasVideo -> MediaType.PHOTO
                    isInstagram && info.webpageUrl?.contains("/stories/") == true -> MediaType.VIDEO
                    isInstagram && info.webpageUrl?.contains("/s/") == true -> MediaType.VIDEO // Highlights
                    info.playlistIndex != null -> MediaType.CAROUSEL
                    isInstagram && !hasVideo -> MediaType.PHOTO
                    else -> MediaType.VIDEO
                }

                val durationFormatted = when {
                    mediaType == MediaType.PHOTO -> "Photo"
                    durationSec > 0 -> durationSec.toDurationText()
                    else -> null
                }

                return ViewState(
                    url = info.originalUrl.toString(),
                    title = info.title,
                    uploader = info.uploader ?: info.channel ?: info.uploaderId.toString(),
                    extractorKey = info.extractorKey,
                    duration = durationSec,
                    durationText = durationFormatted,
                    thumbnailUrl = info.thumbnail.toHttpsUrl(),
                    fileSizeApprox = info.fileSize ?: info.fileSizeApprox ?: .0,
                    videoFormats = videoFormats,
                    audioOnlyFormats = audioOnlyFormats,
                    mediaType = mediaType,
                )
            }
        }
    }

    companion object {
        private const val PROGRESS_INDETERMINATE = -1f
    }
}
