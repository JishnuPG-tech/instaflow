@file:OptIn(ExperimentalMaterial3Api::class)

package com.instaflow.app.ui.page.download

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instaflow.app.App.Companion.applicationScope
import com.instaflow.app.App.Companion.context
import com.instaflow.app.Downloader
import com.instaflow.app.Downloader.State
import com.instaflow.app.Downloader.manageDownloadError
import com.instaflow.app.Downloader.updatePlaylistResult
import com.instaflow.app.R
import com.instaflow.app.util.CUSTOM_COMMAND
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.FORMAT_SELECTION
import com.instaflow.app.util.InstagramCarouselRouter
import com.instaflow.app.util.PLAYLIST
import com.instaflow.app.util.PlaylistResult
import com.instaflow.app.util.PreferenceUtil.getBoolean
import com.instaflow.app.util.ToastUtil
import com.instaflow.app.util.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO: Refactoring for introducing multitasking and download queue management
class HomePageViewModel : ViewModel(), KoinComponent {
    private val downloader: com.instaflow.app.download.DownloaderV2 by inject()

    private val mutableViewStateFlow = MutableStateFlow(ViewState())
    val viewStateFlow = mutableViewStateFlow.asStateFlow()

    val videoInfoFlow = MutableStateFlow(VideoInfo())

    data class ViewState(
        val showPlaylistSelectionDialog: Boolean = false,
        val url: String = "",
        val showFormatSelectionPage: Boolean = false,
        val isUrlSharingTriggered: Boolean = false,
        val showInstagramPreviewSheet: Boolean = false,
        val instagramAuthor: String = "instagram_user",
        val instagramCaption: String = "",
        val instagramThumbnail: String = "",
        val instagramItems: List<com.instaflow.app.database.InstagramMediaItem> = emptyList(),
        val isInstagramCarousel: Boolean = false,
        val targetVideoInfo: VideoInfo? = null,
    )

    fun updateUrl(url: String, isUrlSharingTriggered: Boolean = false) =
        mutableViewStateFlow.update {
            it.copy(url = url, isUrlSharingTriggered = isUrlSharingTriggered)
        }

    fun startDownloadVideo() {
        val url = viewStateFlow.value.url
        Downloader.clearErrorState()
        if (CUSTOM_COMMAND.getBoolean()) {
            applicationScope.launch(Dispatchers.IO) { DownloadUtil.executeCommandInBackground(url) }
            return
        }
        if (!Downloader.isDownloaderAvailable()) return
        if (url.isBlank()) {
            ToastUtil.makeToast(context.getString(R.string.url_empty))
            return
        }

        // Instagram-First Interception: Replace Seal format/playlist pickers for Instagram URLs
        val parsedIgUrl = com.instaflow.app.util.InstagramUrlValidator.parseUrl(url)
        if (parsedIgUrl.isValid) {
            viewModelScope.launch(Dispatchers.IO) { fetchInfoForInstagramSheet(url, parsedIgUrl) }
            return
        }

        if (PLAYLIST.getBoolean()) {
            viewModelScope.launch(Dispatchers.IO) { parsePlaylistInfo(url) }
            return
        }

        if (FORMAT_SELECTION.getBoolean()) {
            viewModelScope.launch(Dispatchers.IO) { fetchInfoForFormatSelection(url) }
            return
        }

        Downloader.getInfoAndDownload(url)
    }

    private fun fetchInfoForInstagramSheet(url: String, parseResult: com.instaflow.app.util.InstagramUrlParseResult) {
        Downloader.updateState(State.FetchingInfo)
        DownloadUtil.getPlaylistOrVideoInfo(url)
            .onSuccess { info ->
                Downloader.updateState(State.Idle)
                when (info) {
                    is PlaylistResult -> {
                        val items = info.entries?.mapIndexed { index, entry ->
                            val isVideoEntry = (entry.duration ?: 0.0) > 0.0
                            com.instaflow.app.database.InstagramMediaItem(
                                id = entry.id ?: "${parseResult.shortcode}_$index",
                                shortcode = parseResult.shortcode ?: "",
                                mediaType = if (isVideoEntry) com.instaflow.app.database.InstagramMediaType.VIDEO else com.instaflow.app.database.InstagramMediaType.IMAGE,
                                downloadUrl = entry.url ?: "",
                                thumbnailUrl = entry.thumbnails?.firstOrNull()?.url ?: entry.url ?: "",
                                authorUsername = info.uploader ?: parseResult.username ?: "instagram_user",
                                caption = info.title,
                                isVideo = isVideoEntry,
                                durationSeconds = entry.duration?.toInt() ?: 0,
                                carouselIndex = index,
                                totalCarouselItems = info.entries?.size ?: 1,
                            )
                        } ?: emptyList()

                        mutableViewStateFlow.update {
                            it.copy(
                                showInstagramPreviewSheet = true,
                                instagramAuthor = info.uploader ?: parseResult.username ?: "instagram_user",
                                instagramCaption = info.title ?: "",
                                instagramThumbnail = items.firstOrNull()?.thumbnailUrl ?: "",
                                instagramItems = items,
                                isInstagramCarousel = true,
                            )
                        }
                    }
                    is VideoInfo -> {
                        mutableViewStateFlow.update {
                            it.copy(
                                showInstagramPreviewSheet = true,
                                instagramAuthor = info.uploader ?: parseResult.username ?: "instagram_user",
                                instagramCaption = info.title ?: "",
                                instagramThumbnail = info.thumbnail ?: "",
                                instagramItems = emptyList(),
                                isInstagramCarousel = false,
                                targetVideoInfo = info,
                            )
                        }
                    }
                }
            }
            .onFailure {
                Downloader.manageDownloadError(th = it, url = url, isFetchingInfo = true, isTaskAborted = true)
                Downloader.updateState(State.Idle)
            }
    }

    fun hideInstagramPreviewSheet() {
        mutableViewStateFlow.update { it.copy(showInstagramPreviewSheet = false) }
    }

    fun downloadInstagramSingle(audioOnly: Boolean = false) {
        val info = viewStateFlow.value.targetVideoInfo
        val url = viewStateFlow.value.url
        hideInstagramPreviewSheet()
        if (info != null) {
            val preferences = DownloadUtil.DownloadPreferences.createFromPreferences().copy(extractAudio = audioOnly)
            Downloader.downloadVideoWithInfo(info = info, preferences = preferences)
        } else if (url.isNotEmpty()) {
            Downloader.getInfoAndDownload(url)
        }
    }

    fun downloadInstagramSelectedItems(selectedItems: List<com.instaflow.app.database.InstagramMediaItem>) {
        hideInstagramPreviewSheet()
        val prefs = DownloadUtil.DownloadPreferences.createFromPreferences()
        val author = viewStateFlow.value.instagramAuthor
        selectedItems.forEachIndexed { index, item ->
            val itemUrl = item.downloadUrl
            if (itemUrl.isNotEmpty()) {
                val task = com.instaflow.app.download.Task(
                    url = itemUrl,
                    preferences = prefs,
                )
                val initialState = com.instaflow.app.download.Task.State(
                    downloadState = com.instaflow.app.download.Task.DownloadState.Idle,
                    videoInfo = null,
                    viewState = com.instaflow.app.download.Task.ViewState(
                        title = "(@$author) Item ${index + 1} of ${selectedItems.size}",
                        thumbnailUrl = item.thumbnailUrl,
                        uploader = author,
                    ),
                )
                downloader.enqueue(task, initialState)
            }
        }
        ToastUtil.makeToast("Enqueued ${selectedItems.size} items for download")
    }

    private fun fetchInfoForFormatSelection(url: String) {
        Downloader.updateState(State.FetchingInfo)
        DownloadUtil.fetchVideoInfoFromUrl(url = url)
            .onSuccess { showFormatSelectionPageOrDownload(it) }
            .onFailure {
                manageDownloadError(th = it, url = url, isFetchingInfo = true, isTaskAborted = true)
            }
        Downloader.updateState(State.Idle)
    }

    private fun parsePlaylistInfo(url: String): Unit =
        Downloader.run {
            if (!isDownloaderAvailable()) return
            clearErrorState()
            updateState(State.FetchingInfo)
            DownloadUtil.getPlaylistOrVideoInfo(url)
                .onSuccess { info ->
                    updateState(State.Idle)
                    when (info) {
                        is PlaylistResult -> {
                            // WP 4.2: Instagram carousels are auto-downloaded without
                            // showing the playlist selection dialog. All other playlist
                            // types (YouTube, SoundCloud, etc.) use the normal path.
                            val carouselResult = InstagramCarouselRouter.routeFromPlaylist(
                                originalUrl = url,
                                playlistResult = info,
                                preferences = DownloadUtil.DownloadPreferences.createFromPreferences(),
                                downloader = downloader,
                            )
                            when (carouselResult) {
                                is InstagramCarouselRouter.RoutingResult.CarouselEnqueued -> {
                                    ToastUtil.makeToast(
                                        "Downloading ${carouselResult.itemCount} items from @${carouselResult.author}"
                                    )
                                }
                                is InstagramCarouselRouter.RoutingResult.ParseFailed -> {
                                    // Fall back to playlist dialog so user isn't stuck.
                                    showPlaylistPage(info)
                                }
                                InstagramCarouselRouter.RoutingResult.SingleItem -> {
                                    // Not an Instagram carousel — show normal playlist selection.
                                    showPlaylistPage(info)
                                }
                            }
                        }

                        is VideoInfo -> {
                            if (FORMAT_SELECTION.getBoolean()) {

                                showFormatSelectionPageOrDownload(info)
                            } else if (isDownloaderAvailable()) {
                                downloadVideoWithInfo(info = info)
                            }
                        }
                    }
                }
                .onFailure {
                    manageDownloadError(
                        th = it,
                        url = url,
                        isFetchingInfo = true,
                        isTaskAborted = true,
                    )
                }
        }

    private fun showPlaylistPage(playlistResult: PlaylistResult) {
        updatePlaylistResult(playlistResult)
        mutableViewStateFlow.update { it.copy(showPlaylistSelectionDialog = true) }
    }

    private fun showFormatSelectionPageOrDownload(info: VideoInfo) {
        if (info.format.isNullOrEmpty()) Downloader.downloadVideoWithInfo(info)
        else {
            videoInfoFlow.update { info }
            mutableViewStateFlow.update { it.copy(showFormatSelectionPage = true) }
        }
    }

    fun hidePlaylistDialog() {
        mutableViewStateFlow.update { it.copy(showPlaylistSelectionDialog = false) }
    }

    fun hideFormatPage() {
        mutableViewStateFlow.update { it.copy(showFormatSelectionPage = false) }
    }

    fun onShareIntentConsumed() {
        mutableViewStateFlow.update { it.copy(isUrlSharingTriggered = false) }
    }

    companion object {
        private const val TAG = "DownloadViewModel"
    }
}
