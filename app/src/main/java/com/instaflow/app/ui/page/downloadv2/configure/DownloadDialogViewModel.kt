package com.instaflow.app.ui.page.downloadv2.configure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.instaflow.app.database.objects.CommandTemplate
import com.instaflow.app.download.DownloaderV2
import com.instaflow.app.download.Task
import com.instaflow.app.features.instagram.ui.ProgressStep
import com.instaflow.app.features.instagram.ui.StepStatus
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.InstagramUrlType
import com.instaflow.app.util.PlaylistResult
import com.instaflow.app.util.VideoInfo
import com.instaflow.app.util.FAST_MODE
import com.instaflow.app.util.PreferenceUtil.getBoolean
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "DownloadDialogViewModel"

class DownloadDialogViewModel(private val downloader: DownloaderV2) : ViewModel() {

    sealed interface SelectionState {
        data object Idle : SelectionState

        data class PlaylistSelection(val result: PlaylistResult) : SelectionState

        data class FormatSelection(val info: VideoInfo) : SelectionState
    }

    sealed interface SheetState {
        data object InputUrl : SheetState

        data class Configure(val urlList: List<String>) : SheetState

        data class InstagramAnalyzing(val url: String, val steps: List<ProgressStep>) : SheetState

        /**
         * Instagram single-item preview ready.
         *
         * @param urlType The [InstagramUrlType] resolved by [InstagramUrlValidator]. Carried through
         *   the pipeline so downstream mappers never have to re-parse the URL.
         */
        data class InstagramPreview(
            val info: VideoInfo,
            val urlType: InstagramUrlType,
            val targetUrl: String,
        ) : SheetState

        data class InstagramCarouselPreview(val result: PlaylistResult) : SheetState

        data class Loading(val taskKey: String, val job: Job) : SheetState

        data class Error(val action: Action, val throwable: Throwable) : SheetState
    }

    sealed interface SheetValue {
        data object Expanded : SheetValue

        data object Hidden : SheetValue
    }

    sealed interface Action {
        data object HideSheet : Action

        data class ShowSheet(val urlList: List<String>? = null) : Action

        data class ProceedWithURLs(val urlList: List<String>) : Action

        data object Reset : Action

        data class FetchPlaylist(
            val url: String,
            val preferences: DownloadUtil.DownloadPreferences,
        ) : Action

        data class FetchFormats(
            val url: String,
            val audioOnly: Boolean,
            val preferences: DownloadUtil.DownloadPreferences,
        ) : Action

        data class DownloadWithPreset(
            val urlList: List<String>,
            val preferences: DownloadUtil.DownloadPreferences,
        ) : Action

        data class RunCommand(
            val url: String,
            val template: CommandTemplate,
            val preferences: DownloadUtil.DownloadPreferences,
        ) : Action

        data class AnalyzeInstagram(val url: String) : Action

        /**
         * Lazy carousel enqueue: fired when the user taps "Download" on the carousel preview sheet.
         * The ViewModel has access to [downloader]; composables do not.
         */
        data class DownloadCarousel(
            val playlistResult: PlaylistResult,
            val selectedIndices: List<Int>,
            val preferences: DownloadUtil.DownloadPreferences,
        ) : Action

        data object Cancel : Action
    }

    private val mSelectionStateFlow: MutableStateFlow<SelectionState> =
        MutableStateFlow(SelectionState.Idle)
    private val mSheetStateFlow: MutableStateFlow<SheetState> =
        MutableStateFlow(SheetState.InputUrl)
    private val mSheetValueFlow: MutableStateFlow<SheetValue> = MutableStateFlow(SheetValue.Hidden)

    val selectionStateFlow = mSelectionStateFlow.asStateFlow()
    val sheetStateFlow = mSheetStateFlow.asStateFlow()
    val sheetValueFlow = mSheetValueFlow.asStateFlow()

    private val sheetState
        get() = sheetStateFlow.value

    fun postAction(action: Action) {
        with(action) {
            when (this) {
                is Action.ProceedWithURLs -> proceedWithUrls(this)
                is Action.FetchFormats -> fetchFormat(this)
                is Action.FetchPlaylist -> fetchPlaylist(this)
                is Action.DownloadWithPreset -> downloadWithPreset(urlList, preferences)
                is Action.DownloadCarousel -> downloadCarousel(this)
                is Action.RunCommand -> runCommand(url, template, preferences)
                is Action.AnalyzeInstagram -> {
                    // Re-derive urlType from the URL when action is dispatched without a type
                    val norm = com.instaflow.app.features.instagram.url.InstagramUrlNormalizer.normalize(this.url)
                    val type = com.instaflow.app.util.InstagramUrlValidator.parseUrl(norm).type
                    analyzeInstagram(norm, type)
                }
                Action.HideSheet -> hideDialog()
                is Action.ShowSheet -> showDialog(this)
                Action.Cancel -> cancel()
                Action.Reset -> resetSelectionState()
            }
        }
    }

    private fun proceedWithUrls(action: Action.ProceedWithURLs) {
        val url = action.urlList.firstOrNull() ?: ""
        val normalized = com.instaflow.app.features.instagram.url.InstagramUrlNormalizer.normalize(url)
        val parseResult = com.instaflow.app.util.InstagramUrlValidator.parseUrl(normalized)
        if (parseResult.isValid) {
            Log.i(TAG, "[Pipeline] proceedWithUrls → Instagram, type=${parseResult.type}, url=$normalized")
            analyzeInstagram(normalized, parseResult.type)
        } else {
            mSheetStateFlow.update { SheetState.Configure(action.urlList) }
        }
    }

    private fun fetchPlaylist(action: Action.FetchPlaylist) {
        val (url, preferences) = action

        val job =
            viewModelScope.launch(Dispatchers.IO) {
                DownloadUtil.getPlaylistOrVideoInfo(
                        playlistURL = url,
                        downloadPreferences = preferences,
                    )
                    .onSuccess { info ->
                        withContext(Dispatchers.Main) {
                            when (info) {
                                is PlaylistResult -> {
                                    mSelectionStateFlow.update {
                                        SelectionState.PlaylistSelection(result = info)
                                    }
                                }

                                is VideoInfo -> {
                                    mSelectionStateFlow.update {
                                        SelectionState.FormatSelection(info = info)
                                    }
                                }
                            }
                            hideDialog()
                        }
                    }
                    .onFailure { th ->
                        mSheetStateFlow.update { SheetState.Error(action = action, throwable = th) }
                    }
            }
        mSheetStateFlow.update { SheetState.Loading(taskKey = "FetchPlaylist_$url", job = job) }
    }

    private fun fetchFormat(action: Action.FetchFormats) {
        val (url, audioOnly, preferences) = action
        val norm = com.instaflow.app.features.instagram.url.InstagramUrlNormalizer.normalize(url)
        val parseResult = com.instaflow.app.util.InstagramUrlValidator.parseUrl(norm)
        if (parseResult.isValid) {
            Log.i(TAG, "[Pipeline] fetchFormat re-routed to Instagram analyzeInstagram, type=${parseResult.type}")
            analyzeInstagram(norm, parseResult.type)
            return
        }

        val job =
            viewModelScope.launch(Dispatchers.IO) {
                DownloadUtil.fetchVideoInfoFromUrl(
                        url = url,
                        preferences = preferences.copy(extractAudio = audioOnly),
                        taskKey = "FetchFormat_$url",
                    )
                    .onSuccess { info ->
                        withContext(Dispatchers.Main) {
                            mSelectionStateFlow.update {
                                SelectionState.FormatSelection(info = info)
                            }
                            hideDialog()
                        }
                    }
                    .onFailure { th ->
                        withContext(Dispatchers.Main) {
                            mSheetStateFlow.update { SheetState.Error(action, throwable = th) }
                        }
                    }
            }

        mSheetStateFlow.update { SheetState.Loading(taskKey = "FetchFormat_$url", job = job) }
    }

    private fun downloadWithPreset(
        urlList: List<String>,
        preferences: DownloadUtil.DownloadPreferences,
    ) {
        urlList.filter { it.isNotBlank() }.forEach { downloader.enqueue(Task(url = it, preferences = preferences)) }
        hideDialog()
    }

    /**
     * Phase 4 — Lazy carousel enqueue.
     * Called when the user taps "Download" on the carousel preview sheet.
     * Uses [InstagramCarouselRouter] to create rich per-item [Task]s in [downloader].
     * Falls back to raw URL enqueue if the router cannot parse the playlist.
     */
    private fun downloadCarousel(action: Action.DownloadCarousel) {
        val originalUrl = action.playlistResult.webpageUrl ?: action.playlistResult.originalUrl ?: ""
        Log.i(TAG, "[Pipeline] downloadCarousel: enqueuing ${action.selectedIndices.size} selected items for $originalUrl")
        
        // Map indices to URLs for fallback
        val entries = action.playlistResult.entries.orEmpty()
        val selectedUrls = action.selectedIndices.mapNotNull { index ->
            if (index >= 0 && index < entries.size) {
                val entry = entries[index]
                entry.url ?: entry.webpageUrl ?: entry.originalUrl ?: ""
            } else null
        }.filter { it.isNotBlank() }

        val routingResult = com.instaflow.app.util.InstagramCarouselRouter.routeFromPlaylist(
            originalUrl = originalUrl,
            playlistResult = action.playlistResult,
            preferences = action.preferences,
            downloader = downloader,
            selectedIndices = action.selectedIndices,
        )
        when (routingResult) {
            is com.instaflow.app.util.InstagramCarouselRouter.RoutingResult.CarouselEnqueued -> {
                Log.i(TAG, "[Pipeline] carousel enqueued: ${routingResult.itemCount} tasks via router")
            }
            com.instaflow.app.util.InstagramCarouselRouter.RoutingResult.SingleItem -> {
                Log.i(TAG, "[Pipeline] carousel fell through to single item, enqueuing ${selectedUrls.size} raw URLs")
                selectedUrls.forEach { downloader.enqueue(Task(url = it, preferences = action.preferences)) }
            }
            is com.instaflow.app.util.InstagramCarouselRouter.RoutingResult.ParseFailed -> {
                Log.w(TAG, "[Pipeline] carousel router ParseFailed: ${routingResult.reason} — falling back to raw URLs")
                selectedUrls.forEach { downloader.enqueue(Task(url = it, preferences = action.preferences)) }
            }
        }
        hideDialog()
    }

    private fun runCommand(
        url: String,
        template: CommandTemplate,
        preferences: DownloadUtil.DownloadPreferences,
    ) {
        val task =
            Task(
                url = url,
                type = Task.TypeInfo.CustomCommand(template = template),
                preferences = preferences,
            )
        downloader.enqueue(task)
    }

    private fun analyzeInstagram(url: String, urlType: InstagramUrlType) {
        Log.i(TAG, "[Pipeline] ─────────────────────────────────────")
        Log.i(TAG, "[Pipeline] URL received     : $url")
        Log.i(TAG, "[Pipeline] URL type         : $urlType")
        val startMs = System.currentTimeMillis()

        val initialSteps = listOf(
            ProgressStep("Validating URL"),
            ProgressStep("Resolving shortcode"),
            ProgressStep("Reading metadata"),
            ProgressStep("Reading media"),
            ProgressStep("Reading formats"),
            ProgressStep("Loading preview")
        )

        val job = viewModelScope.launch(Dispatchers.IO) {
            try {
                val taskKey = "IG_ANALYZE_${System.currentTimeMillis()}"

                // Step 1: Validating
                updateAnalyzingSteps(initialSteps.mapIndexed { i, s -> if (i == 0) s.copy(status = StepStatus.IN_PROGRESS) else s })
                val normalized = com.instaflow.app.features.instagram.url.InstagramUrlNormalizer.normalize(url)
                Log.i(TAG, "[Pipeline] Normalized URL    : $normalized")
                val parseResult = com.instaflow.app.util.InstagramUrlValidator.parseUrl(normalized)
                if (!parseResult.isValid) throw Exception("Invalid Instagram URL")
                Log.i(TAG, "[Pipeline] Classified as     : ${parseResult.type} (shortcode=${parseResult.shortcode}, username=${parseResult.username})")

                // Step 2: Resolving
                updateAnalyzingSteps(initialSteps.mapIndexed { i, s ->
                    when(i) {
                        0 -> s.copy(status = StepStatus.COMPLETED)
                        1 -> s.copy(status = StepStatus.IN_PROGRESS)
                        else -> s
                    }
                })
                delay(300)

                // Step 3: Reading metadata (Calling server/yt-dlp)
                withContext(Dispatchers.Main) {
                    updateAnalyzingSteps(initialSteps.mapIndexed { i, s ->
                        when(i) {
                            0, 1 -> s.copy(status = StepStatus.COMPLETED)
                            2 -> s.copy(status = StepStatus.IN_PROGRESS)
                            else -> s
                        }
                    })
                }

                val prefs = DownloadUtil.DownloadPreferences.createFromPreferences()
                Log.i(TAG, "[Pipeline] Calling server/yt-dlp   : taskKey=$taskKey")
                val ytDlpStart = System.currentTimeMillis()

                DownloadUtil.getPlaylistOrVideoInfo(playlistURL = normalized, downloadPreferences = prefs, taskKey = taskKey)
                    .onSuccess { info ->
                        val extractionMs = System.currentTimeMillis() - ytDlpStart
                        Log.i(TAG, "[Pipeline] Extraction success: ${extractionMs}ms")

                        viewModelScope.launch(Dispatchers.Main) {
                            // Step 4: Reading media
                            updateAnalyzingSteps(initialSteps.mapIndexed { i, s ->
                                when(i) {
                                    in 0..2 -> s.copy(status = StepStatus.COMPLETED)
                                    3 -> s.copy(status = StepStatus.IN_PROGRESS)
                                    else -> s
                                }
                            })
                            delay(600)

                            // Step 5: Reading formats
                            updateAnalyzingSteps(initialSteps.mapIndexed { i, s ->
                                when(i) {
                                    in 0..3 -> s.copy(status = StepStatus.COMPLETED)
                                    4 -> s.copy(status = StepStatus.IN_PROGRESS)
                                    else -> s
                                }
                            })
                            delay(500)

                            if (info is VideoInfo) {
                                com.instaflow.app.features.instagram.repository.InstagramQualityRepository.validateVideoInfo(info)
                                val uiModel = com.instaflow.app.features.instagram.repository.InstagramHandlerDispatch.handle(info, urlType)
                                
                                val isFastMode = FAST_MODE.getBoolean()
                                if (isFastMode && urlType != InstagramUrlType.CAROUSEL) {
                                    val bestFormat = uiModel.videoQualityOptions.firstOrNull() ?: uiModel.audioQualityOptions.firstOrNull()
                                    val p = prefs.copy(
                                        formatIdString = bestFormat?.formatId ?: "",
                                        extractAudio = bestFormat?.isAudioOnly ?: false,
                                        mergePhotoAudio = bestFormat?.mergePhotoAudio ?: false
                                    )
                                    downloader.enqueue(Task(url = info.webpageUrl ?: normalized, preferences = p))
                                    hideDialog()
                                    return@launch
                                }

                                // Step 6: Loading preview
                                updateAnalyzingSteps(initialSteps.mapIndexed { i, s ->
                                    when(i) {
                                        in 0..4 -> s.copy(status = StepStatus.COMPLETED)
                                        5 -> s.copy(status = StepStatus.IN_PROGRESS)
                                        else -> s
                                    }
                                })
                                delay(400)

                                updateAnalyzingSteps(initialSteps.map { it.copy(status = StepStatus.COMPLETED) })
                                delay(200)
                                
                                val targetUrl = info.webpageUrl?.ifBlank { null } ?: info.originalUrl?.ifBlank { null } ?: normalized
                                mSheetStateFlow.update { SheetState.InstagramPreview(info, urlType, targetUrl) }
                            } else if (info is PlaylistResult) {
                                // Step 6: Loading preview for Carousel
                                updateAnalyzingSteps(initialSteps.mapIndexed { i, s ->
                                    when(i) {
                                        in 0..4 -> s.copy(status = StepStatus.COMPLETED)
                                        5 -> s.copy(status = StepStatus.IN_PROGRESS)
                                        else -> s
                                    }
                                })
                                delay(500)

                                updateAnalyzingSteps(initialSteps.map { it.copy(status = StepStatus.COMPLETED) })
                                delay(200)
                                mSheetStateFlow.update { SheetState.InstagramCarouselPreview(info) }
                            }
                        }
                    }
                    .onFailure { th ->
                        val extractionMs = System.currentTimeMillis() - ytDlpStart
                        Log.e(TAG, "[Pipeline] yt-dlp FAILED    : ${extractionMs}ms — ${th.message}")
                        withContext(Dispatchers.Main) {
                            val msg = th.message ?: ""
                            val refinedError = when {
                                msg.contains("no video", ignoreCase = true) || msg.contains("no format", ignoreCase = true) ->
                                    Exception("This post might be a static photo or restricted. If the issue persists, please log in to Instagram in Settings > Network.")
                                msg.contains("empty media response", ignoreCase = true) || msg.contains("Login required", ignoreCase = true) || msg.contains("Private", ignoreCase = true) ->
                                    Exception("Login required for this content. Please go to Settings > Network and log in to your Instagram account.")
                                else -> th
                            }
                            mSheetStateFlow.update { SheetState.Error(Action.AnalyzeInstagram(url), refinedError) }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "[Pipeline] analyzeInstagram caught exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    mSheetStateFlow.update { SheetState.Error(Action.AnalyzeInstagram(url), e) }
                }
            }
        }
        mSheetStateFlow.update { SheetState.InstagramAnalyzing(url, initialSteps) }
        mSheetValueFlow.update { SheetValue.Expanded }
    }

    private fun updateAnalyzingSteps(steps: List<ProgressStep>) {
        mSheetStateFlow.update { 
            if (it is SheetState.InstagramAnalyzing) it.copy(steps = steps) else it
        }
    }

    private fun hideDialog() {
        mSheetValueFlow.update { SheetValue.Hidden }
        mSheetStateFlow.update { SheetState.InputUrl }
        when (sheetState) {
            is SheetState.Loading -> {
                cancel()
            }

            else -> {}
        }
    }

    private fun showDialog(action: Action.ShowSheet) {
        val urlList = action.urlList
        if (!urlList.isNullOrEmpty()) {
            val url = urlList.first()
            val normalized = com.instaflow.app.features.instagram.url.InstagramUrlNormalizer.normalize(url)
            val parseResult = com.instaflow.app.util.InstagramUrlValidator.parseUrl(normalized)
            if (parseResult.isValid) {
                Log.i(TAG, "[Pipeline] showDialog → Instagram, type=${parseResult.type}, url=$normalized")
                analyzeInstagram(normalized, parseResult.type)
            } else {
                mSheetStateFlow.update { SheetState.Configure(urlList) }
                mSheetValueFlow.update { SheetValue.Expanded }
            }
        } else {
            mSheetStateFlow.update { SheetState.InputUrl }
            mSheetValueFlow.update { SheetValue.Expanded }
        }
    }

    private fun cancel(): Boolean {
        return when (val state = sheetState) {
            is SheetState.Loading -> {
                val res = YoutubeDL.destroyProcessById(id = state.taskKey)
                if (res) {
                    state.job.cancel()
                }
                return res
            }
            else -> false
        }
    }

    private fun resetSelectionState() {
        mSelectionStateFlow.update { SelectionState.Idle }
    }
}
