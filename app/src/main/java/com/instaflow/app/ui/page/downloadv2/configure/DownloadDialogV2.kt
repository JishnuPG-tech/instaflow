package com.instaflow.app.ui.page.downloadv2.configure

import androidx.activity.compose.BackHandler
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NewLabel
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instaflow.app.App
import com.instaflow.app.R
import com.instaflow.app.ui.common.Route
import com.instaflow.app.ui.common.HapticFeedback.longPressHapticFeedback
import androidx.compose.material.icons.outlined.PersonAdd
import com.instaflow.app.ui.common.motion.materialSharedAxisX
import com.instaflow.app.ui.component.ButtonChip
import com.instaflow.app.ui.component.DrawerSheetSubtitle
import com.instaflow.app.ui.component.OutlinedButtonWithIcon
import com.instaflow.app.ui.component.InstaFlowModalBottomSheet
import com.instaflow.app.ui.component.InstaFlowModalBottomSheetM2Variant
import com.instaflow.app.ui.component.SingleChoiceChip
import com.instaflow.app.ui.component.SingleChoiceSegmentedButton
import com.instaflow.app.ui.component.VideoFilterChip
import com.instaflow.app.ui.page.command.TemplatePickerDialog
import com.instaflow.app.ui.page.downloadv2.configure.ActionButton.Download
import com.instaflow.app.ui.page.downloadv2.configure.ActionButton.FetchInfo
import com.instaflow.app.ui.page.downloadv2.configure.ActionButton.StartTask
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.SelectionState
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.Configure
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.Error
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.InputUrl
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.SheetState.Loading
import com.instaflow.app.ui.page.settings.command.CommandTemplateDialog
import com.instaflow.app.ui.page.settings.format.AudioQuickSettingsDialog
import com.instaflow.app.ui.page.settings.format.VideoQuickSettingsDialog
import com.instaflow.app.ui.page.settings.network.AccountsQuickSettingsDialog
import com.instaflow.app.ui.theme.InstaFlowTheme
import com.instaflow.app.util.ACCOUNTS
import com.instaflow.app.util.AUDIO_CONVERSION_FORMAT
import com.instaflow.app.util.AUDIO_CONVERT
import com.instaflow.app.util.AUDIO_FORMAT
import com.instaflow.app.util.AUDIO_QUALITY
import com.instaflow.app.util.CUSTOM_COMMAND
import com.instaflow.app.util.DatabaseUtil
import com.instaflow.app.util.DownloadType
import com.instaflow.app.util.DownloadType.Audio
import com.instaflow.app.util.DownloadType.Command
import com.instaflow.app.util.DownloadType.Post
import com.instaflow.app.util.DownloadType.Video
import com.instaflow.app.util.DownloadType.entries
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.FORMAT_SELECTION
import com.instaflow.app.util.PLAYLIST
import com.instaflow.app.util.PreferenceStrings
import com.instaflow.app.util.PreferenceUtil
import com.instaflow.app.util.PreferenceUtil.getBoolean
import com.instaflow.app.util.PreferenceUtil.updateBoolean
import com.instaflow.app.util.PreferenceUtil.updateInt
import com.instaflow.app.util.SUBTITLE
import com.instaflow.app.util.TEMPLATE_ID
import com.instaflow.app.util.THUMBNAIL
import com.instaflow.app.util.ToastUtil
import com.instaflow.app.util.USE_CUSTOM_AUDIO_PRESET
import com.instaflow.app.util.VIDEO_FORMAT
import com.instaflow.app.util.VIDEO_QUALITY
import kotlinx.coroutines.launch

@Composable
private fun DownloadType.label(): String =
    stringResource(
        when (this) {
            Audio -> R.string.audio
            Video -> R.string.video
            Post -> R.string.posts
            Command -> R.string.commands
        }
    )

val PreferencesMock = DownloadUtil.DownloadPreferences.EMPTY

data class Config(
    val downloadType: DownloadType? = PreferenceUtil.getDownloadType(),
    val typeEntries: List<DownloadType> =
        when (CUSTOM_COMMAND.getBoolean()) {
            true -> DownloadType.entries
            false -> DownloadType.entries - Command
        },
    val useFormatSelection: Boolean = FORMAT_SELECTION.getBoolean(),
    val savedLinks: Set<String> = PreferenceUtil.getSavedLinks(),
) {
    companion object {
        fun updatePreferences(newValue: Config, oldValue: Config) {
            with(newValue) {
                if (downloadType != oldValue.downloadType) {
                    downloadType?.let { PreferenceUtil.updateDownloadType(it) }
                }
                if (useFormatSelection != oldValue.useFormatSelection) {
                    FORMAT_SELECTION.updateBoolean(useFormatSelection)
                }
                if (savedLinks != oldValue.savedLinks) {
                    PreferenceUtil.updateSavedLinks(savedLinks)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDialog(
    modifier: Modifier = Modifier,
    config: Config,
    sheetState: SheetState,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    state: DownloadDialogViewModel.SheetState = InputUrl,
    onActionPost: (Action) -> Unit = {},
) {
    var showVideoPresetDialog by remember { mutableStateOf(false) }
    var showAudioPresetDialog by remember { mutableStateOf(false) }

    InstaFlowModalBottomSheet(
        sheetState = sheetState,
        contentPadding = PaddingValues(),
        onDismissRequest = { onActionPost(Action.HideSheet) },
    ) {
        DownloadDialogContent(
            modifier = modifier,
            state = state,
            config = config,
            preferences = preferences,
            onPreferencesUpdate = onPreferencesUpdate,
            onPresetEdit = { type ->
                when (type) {
                    Audio -> showAudioPresetDialog = true

                    Video -> showVideoPresetDialog = true

                    else -> {}
                }
            },
            onActionPost = onActionPost,
        )
    }

    if (showVideoPresetDialog) {
        var res by remember(preferences) { mutableIntStateOf(preferences.videoResolution) }
        var format by remember(preferences) { mutableIntStateOf(preferences.videoFormat) }

        VideoQuickSettingsDialog(
            videoResolution = res,
            videoFormatPreference = format,
            onResolutionSelect = { res = it },
            onFormatSelect = { format = it },
            onDismissRequest = { showVideoPresetDialog = false },
            onSave = {
                VIDEO_FORMAT.updateInt(format)
                VIDEO_QUALITY.updateInt(res)
                onPreferencesUpdate(DownloadUtil.DownloadPreferences.createFromPreferences())
            },
        )
    }

    if (showAudioPresetDialog) {
        var quality by remember(preferences) { mutableIntStateOf(preferences.audioQuality) }
        var customPreset by
            remember(preferences) { mutableStateOf(preferences.useCustomAudioPreset) }
        var conversionFmt by
            remember(preferences) { mutableIntStateOf(preferences.audioConvertFormat) }
        var convertAudio by remember(preferences) { mutableStateOf(preferences.convertAudio) }
        var preferredFormat by remember(preferences) { mutableIntStateOf(preferences.audioFormat) }

        AudioQuickSettingsDialog(
            modifier = Modifier,
            preferences = preferences,
            audioQuality = quality,
            onQualitySelect = { quality = it },
            useCustomAudioPreset = customPreset,
            onCustomPresetToggle = { customPreset = it },
            convertAudio = convertAudio,
            onConvertToggled = { convertAudio = it },
            conversionFormat = conversionFmt,
            onConversionSelect = { conversionFmt = it },
            preferredFormat = preferredFormat,
            onPreferredSelect = { preferredFormat = it },
            onDismissRequest = { showAudioPresetDialog = false },
            onSave = {
                AUDIO_QUALITY.updateInt(quality)
                USE_CUSTOM_AUDIO_PRESET.updateBoolean(customPreset)
                AUDIO_CONVERSION_FORMAT.updateInt(conversionFmt)
                AUDIO_CONVERT.updateBoolean(convertAudio)
                AUDIO_FORMAT.updateInt(preferredFormat)
                onPreferencesUpdate(DownloadUtil.DownloadPreferences.createFromPreferences())
            },
        )
    }
}

@Composable
private fun ErrorPage(modifier: Modifier = Modifier, state: Error, onActionPost: (Action) -> Unit) {
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val url =
        state.action.run {
            when (this) {
                is Action.FetchFormats -> url
                is Action.FetchPlaylist -> url
                is Action.AnalyzeInstagram -> url
                else -> ""
            }
        }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.fetch_info_error_msg),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = state.throwable.message.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            maxLines = 20,
            overflow = TextOverflow.Clip,
        )

        Row(modifier = Modifier.padding(top = 8.dp)) {
            FilledTonalButton(onClick = { onActionPost(state.action) }) { Text("Retry") }
            Spacer(Modifier.width(8.dp))
            if (url.contains("instagram.com") || state.throwable.message?.contains("Login", ignoreCase = true) == true) {
                Button(
                    onClick = {
                        onActionPost(Action.HideSheet)
                        // Need to navigate to Cookie Profile. We don't have navController here.
                        // But we can notify the user or use a callback.
                        // For now, let's just make it clear they need to go to settings.
                        ToastUtil.makeToast("Please go to Menu > Instagram Account to log in.")
                    }
                ) {
                    Icon(Icons.Outlined.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Connect Account")
                }
                Spacer(Modifier.width(8.dp))
            }
            Button(
                onClick = {
                    view.longPressHapticFeedback()
                    clipboardManager.setText(
                        AnnotatedString(
                            App.getVersionReport() + "\nURL: ${url}\n${state.throwable.message}"
                        )
                    )
                    ToastUtil.makeToast(R.string.error_copied)
                }
            ) {
                Text(stringResource(R.string.copy_error_report))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DownloadDialogContent(
    modifier: Modifier = Modifier,
    state: DownloadDialogViewModel.SheetState,
    config: Config,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onPresetEdit: (DownloadType?) -> Unit,
    onActionPost: (Action) -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = state,
        label = "",
        transitionSpec = {
            materialSharedAxisX(initialOffsetX = { it / 4 }, targetOffsetX = { -it / 4 })
        },
    ) { state ->
        when (state) {
            is Configure -> {
                check(state.urlList.isNotEmpty())
                if (state.urlList.size == 1) {
                    ConfigurePage(
                        url = state.urlList.first(),
                        config = config,
                        preferences = preferences,
                        onPresetEdit = onPresetEdit,
                        onConfigSave = {
                            Config.updatePreferences(newValue = it, oldValue = config)
                        },
                        settingChips = {
                            AdditionalSettings(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                isQuickDownload = false,
                                preference = preferences,
                                selectedType = config.downloadType,
                                onPreferenceUpdate = {
                                    onPreferencesUpdate(
                                        DownloadUtil.DownloadPreferences.createFromPreferences()
                                    )
                                },
                            )
                        },
                        onActionPost = { onActionPost(it) },
                    )
                } else {
                    ConfigurePagePlaylistVariant(
                        initialDownloadType = config.downloadType ?: Video,
                        preferences = preferences,
                        onPreferencesUpdate = onPreferencesUpdate,
                        onPresetEdit = onPresetEdit,
                        onDismissRequest = { onActionPost(Action.HideSheet) },
                    ) {
                        onActionPost(
                            Action.DownloadWithPreset(
                                urlList = state.urlList,
                                preferences = preferences.copy(extractAudio = it == Audio),
                            )
                        )
                    }
                }
            }

            is DownloadDialogViewModel.SheetState.InstagramAnalyzing -> {
                com.instaflow.app.features.instagram.ui.InstagramAnalyzeScreen(
                    url = state.url,
                    steps = state.steps,
                    onCancel = { onActionPost(Action.HideSheet) }
                )
            }

            is DownloadDialogViewModel.SheetState.InstagramPreview -> {
                val info = state.info
                val model = com.instaflow.app.features.instagram.repository.InstagramHandlerDispatch
                    .handle(info, urlType = state.urlType)
                com.instaflow.app.ui.component.InstagramMediaPreviewSheet(
                    author = model.authorHandle.removePrefix("@"),
                    caption = model.caption,
                    thumbnailUrl = model.thumbnailUrl,
                    mediaTypeLabel = model.mediaTypeLabel,
                    duration = model.durationFormatted,
                    urlType = state.urlType,
                    videoQualityOptions = model.videoQualityOptions,
                    audioQualityOptions = model.audioQualityOptions,
                    onDismissRequest = { onActionPost(Action.HideSheet) },
                    onDownloadWithFormat = { format ->
                        val p = preferences.copy(
                            formatIdString = format.formatId, 
                            extractAudio = format.isAudioOnly,
                            mergePhotoAudio = format.mergePhotoAudio
                        )
                        val downloadUrl = info.webpageUrl?.ifBlank { null }
                            ?: info.originalUrl?.ifBlank { null }
                            ?: state.targetUrl
                        Log.i("DownloadDialogV2", "[Pipeline] Single item download requested — url=$downloadUrl, formatId='${format.formatId}', merge=${format.mergePhotoAudio}")
                        onActionPost(Action.DownloadWithPreset(listOf(downloadUrl), p))
                    }
                )
            }

            is DownloadDialogViewModel.SheetState.InstagramCarouselPreview -> {
                val info = state.result
                val items = info.entries?.mapIndexed { index, entry ->
                    val isVideoEntry = (entry.duration ?: 0.0) > 0.0
                    val entryId = entry.id
                    
                    val directUrl = entry.formats?.maxByOrNull { (it.width ?: 0.0) * (it.height ?: 0.0) }?.url
                    
                    val itemUrl = if (isVideoEntry && !entryId.isNullOrBlank()) {
                        "https://www.instagram.com/p/$entryId/"
                    } else {
                        directUrl ?: entry.url ?: entry.webpageUrl ?: entry.originalUrl ?: 
                        if (!entryId.isNullOrBlank()) "https://www.instagram.com/p/$entryId/" else ""
                    }
                    
                    com.instaflow.app.database.InstagramMediaItem(
                        id = entryId ?: "${info.originalUrl ?: info.webpageUrl ?: "item"}_$index",
                        shortcode = entryId ?: "",
                        mediaType = if (isVideoEntry) com.instaflow.app.database.InstagramMediaType.VIDEO else com.instaflow.app.database.InstagramMediaType.IMAGE,
                        downloadUrl = itemUrl,
                        thumbnailUrl = entry.thumbnails?.firstOrNull()?.url ?: directUrl ?: entry.url ?: "",
                        authorUsername = info.uploader ?: "instagram_user",
                        caption = info.title,
                        isVideo = isVideoEntry,
                        durationSeconds = entry.duration?.toInt() ?: 0,
                        carouselIndex = index,
                        totalCarouselItems = info.entries.size,
                    )
                } ?: emptyList()

                com.instaflow.app.ui.component.InstagramMediaPreviewSheet(
                    author = info.uploader ?: "instagram_user",
                    caption = info.title ?: "",
                    mediaTypeLabel = "Carousel Post",
                    items = items,
                    isCarousel = true,
                    onDismissRequest = { onActionPost(Action.HideSheet) },
                    onDownloadSelectedItems = { selectedItems ->
                        onActionPost(
                            Action.DownloadCarousel(
                                playlistResult = info,
                                selectedIndices = selectedItems.map { it.carouselIndex },
                                preferences = preferences,
                            )
                        )
                    }
                )
            }

            is Error -> {
                ErrorPage(state = state, onActionPost = onActionPost)
            }

            is Loading -> {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 120.dp)) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            InputUrl -> {
                InputUrlPage(
                    config = config,
                    onConfigUpdate = { Config.updatePreferences(newValue = it, oldValue = config) },
                    onActionPost = onActionPost,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ErrorPreview() {
    InstaFlowModalBottomSheet(
        onDismissRequest = {},
        sheetState =
            with(LocalDensity.current) {
                SheetState(
                    initialValue = SheetValue.Expanded,
                    skipPartiallyExpanded = true,
                    velocityThreshold = { 56.dp.toPx() },
                    positionalThreshold = { 125.dp.toPx() },
                )
            },
    ) {
        ErrorPage(
            state =
                Error(
                    action =
                        Action.FetchFormats(
                            url = "",
                            audioOnly = true,
                            preferences = PreferencesMock,
                        ),
                    throwable = Exception("Not good"),
                ),
            onActionPost = {},
        )
    }
}

@Composable
fun FormatPage(
    modifier: Modifier = Modifier,
    state: SelectionState.FormatSelection,
    onDismissRequest: () -> Unit,
) {
    val sheetState =
        androidx.compose.material.rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
        )

    LaunchedEffect(state) { sheetState.show() }
    val scope = rememberCoroutineScope()
    BackHandler { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() } }

    InstaFlowModalBottomSheetM2Variant(sheetState = sheetState, sheetGesturesEnabled = false) {
        FormatPage(
            modifier = modifier,
            videoInfo = state.info,
            onNavigateBack = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigurePage(
    modifier: Modifier = Modifier,
    url: String = "",
    config: Config,
    preferences: DownloadUtil.DownloadPreferences,
    settingChips: @Composable () -> Unit,
    onPresetEdit: (DownloadType?) -> Unit = {},
    onConfigSave: (Config) -> Unit,
    onActionPost: (Action) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var selectedType by remember(config) { mutableStateOf(config.downloadType) }
    var useFormatSelection by remember(config) { mutableStateOf(config.useFormatSelection) }
    val canProceed = selectedType in config.typeEntries

    var showTemplateSelectionDialog by remember { mutableStateOf(false) }
    var showTemplateCreatorDialog by remember { mutableStateOf(false) }
    var showTemplateEditorDialog by remember { mutableStateOf(false) }
    val template by
        remember(showTemplateCreatorDialog, showTemplateSelectionDialog, showTemplateEditorDialog) {
            mutableStateOf(PreferenceUtil.getTemplate())
        }

    LaunchedEffect(selectedType) {
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(modifier = modifier.padding(horizontal = 20.dp)) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 5.dp)
                    .alpha(0.5f),
                contentAlignment = Alignment.Center
            ) {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.onSurfaceVariant) {}
            }

            Header(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                title = "Configure before download",
                icon = Icons.Outlined.DoneAll,
            )
            
            Text(
                text = "Adjust this download",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            DrawerSheetSubtitle(text = "Download type")
            DownloadTypeSelectionGroup(
                typeEntries = config.typeEntries,
                selectedType = selectedType,
                onSelect = { selectedType = it },
            )
            
            Column(modifier = Modifier.animateContentSize()) {
                if (selectedType != Command) {
                    DrawerSheetSubtitle(text = "Format selection")
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        SingleChoiceSegmentedButton(
                            selected = !useFormatSelection,
                            onClick = { useFormatSelection = false },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) {
                            Text(text = "Auto")
                        }
                        SingleChoiceSegmentedButton(
                            selected = useFormatSelection,
                            onClick = { useFormatSelection = true },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) {
                            Text(text = "Custom")
                        }
                    }

                    if (useFormatSelection) {
                        DrawerSheetSubtitle(text = "Format preference")
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ButtonChip(
                                icon = Icons.Outlined.VideoFile,
                                label = "Quality",
                                onClick = { onPresetEdit(Video) }
                            )
                            ButtonChip(
                                icon = Icons.Outlined.HighQuality,
                                label = PreferenceStrings.getVideoResolutionDesc(preferences.videoResolution),
                                onClick = { onPresetEdit(Video) }
                            )
                            ButtonChip(
                                icon = Icons.Outlined.AudioFile,
                                label = "Audio format",
                                onClick = { onPresetEdit(Audio) }
                            )
                        }
                    }
                } else {
                    if (showTemplateSelectionDialog) {
                        TemplatePickerDialog { showTemplateSelectionDialog = false }
                    }
                    if (showTemplateCreatorDialog) {
                        CommandTemplateDialog(
                            onDismissRequest = { showTemplateCreatorDialog = false },
                            confirmationCallback = { scope.launch { TEMPLATE_ID.updateInt(it) } },
                        )
                    }
                    if (showTemplateEditorDialog) {
                        CommandTemplateDialog(
                            commandTemplate = template,
                            onDismissRequest = { showTemplateEditorDialog = false },
                        )
                    }
                    DrawerSheetSubtitle(
                        text = stringResource(id = R.string.template_selection),
                        modifier = Modifier,
                    )
                    LazyRow(modifier = Modifier) {
                        item {
                            ButtonChip(
                                icon = Icons.Outlined.Code,
                                label = template.name,
                                onClick = { showTemplateSelectionDialog = true },
                            )
                        }
                        item {
                            ButtonChip(
                                icon = Icons.Outlined.NewLabel,
                                label = stringResource(id = R.string.new_template),
                                onClick = { showTemplateCreatorDialog = true },
                            )
                        }
                        item {
                            ButtonChip(
                                icon = Icons.Outlined.Edit,
                                label = stringResource(id = R.string.edit_template, template.name),
                                onClick = { showTemplateEditorDialog = true },
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        DrawerSheetSubtitle(text = "Additional settings", modifier = Modifier.padding(horizontal = 24.dp))
        settingChips()

        ActionButtons(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            canProceed = canProceed,
            selectedType = selectedType,
            useFormatSelection = useFormatSelection,
            onCancel = { onActionPost(Action.HideSheet) },
            onDownload = {
                onConfigSave(
                    config.copy(
                        useFormatSelection = useFormatSelection,
                        downloadType = selectedType,
                    )
                )
                onActionPost(
                    Action.DownloadWithPreset(
                        urlList = listOf(url),
                        preferences = preferences.copy(extractAudio = selectedType == Audio),
                    )
                )
            },
            onFetchInfo = {
                onConfigSave(
                    config.copy(
                        useFormatSelection = useFormatSelection,
                        downloadType = selectedType,
                    )
                )
                onActionPost(
                    Action.FetchFormats(
                        url = url,
                        audioOnly = selectedType == Audio,
                        preferences = preferences,
                    )
                )
            },
            onTaskStart = {
                onConfigSave(
                    config.copy(
                        useFormatSelection = useFormatSelection,
                        downloadType = selectedType,
                    )
                )
                onActionPost(
                    Action.RunCommand(url = url, template = template, preferences = preferences)
                )
            },
        )
    }
}

@Composable
fun ConfigurePagePlaylistVariant(
    modifier: Modifier = Modifier,
    initialDownloadType: DownloadType,
    preferences: DownloadUtil.DownloadPreferences,
    onPreferencesUpdate: (DownloadUtil.DownloadPreferences) -> Unit,
    onPresetEdit: (DownloadType?) -> Unit = {},
    onDismissRequest: () -> Unit,
    onDownload: (DownloadType) -> Unit,
) {

    var selectedType by remember(initialDownloadType) { mutableStateOf(initialDownloadType) }

    Column {
        Column(modifier = modifier.padding(horizontal = 20.dp)) {
            Header(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                title = "Configure before download",
                icon = Icons.Outlined.DoneAll,
            )
            DrawerSheetSubtitle(text = stringResource(id = R.string.download_type))
            DownloadTypeSelectionGroup(
                typeEntries = listOf(Video, Audio),
                selectedType = selectedType,
                onSelect = { selectedType = it },
            )
            DrawerSheetSubtitle(
                text = stringResource(id = R.string.format_selection),
                modifier = Modifier,
            )
            Preset(
                modifier = Modifier,
                preference = preferences,
                selected = true,
                downloadType = selectedType,
                onClick = { onPresetEdit(selectedType) },
                showEditIcon = true,
                onEdit = { onPresetEdit(selectedType) },
            )
        }
        var expanded by remember { mutableStateOf(false) }
        ExpandableTitle(expanded = expanded, onClick = { expanded = true }) {
            AdditionalSettings(
                modifier = Modifier.padding(horizontal = 16.dp),
                isQuickDownload = false,
                preference = preferences,
                selectedType = Audio,
                onPreferenceUpdate = {
                    onPreferencesUpdate(DownloadUtil.DownloadPreferences.createFromPreferences())
                },
            )
        }

        ActionButtons(
            modifier = Modifier.padding(horizontal = 20.dp),
            canProceed = true,
            selectedType = selectedType,
            useFormatSelection = false,
            onCancel = onDismissRequest,
            onDownload = {
                onDownload(initialDownloadType)
                onDismissRequest()
            },
            onFetchInfo = { throw IllegalStateException() },
            onTaskStart = { throw IllegalStateException() },
        )
    }
}

@Composable
private fun AdditionalSettings(
    modifier: Modifier = Modifier,
    isQuickDownload: Boolean,
    selectedType: DownloadType?,
    preference: DownloadUtil.DownloadPreferences,
    onNavigateToAccountGeneratorPage: (String) -> Unit = {},
    onPreferenceUpdate: () -> Unit,
) {
    val accountsProfiles by DatabaseUtil.getAccountsFlow().collectAsStateWithLifecycle(emptyList())
    var showAccountsDialog by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = {
                PLAYLIST.updateBoolean(!preference.downloadPlaylist)
                onPreferenceUpdate()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(44.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (preference.downloadPlaylist) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
            )
        ) {
            Text("Download playlist", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = {
                SUBTITLE.updateBoolean(!preference.downloadSubtitle)
                onPreferenceUpdate()
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(44.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (preference.downloadSubtitle) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
            )
        ) {
            Text("Download subtitles", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }

        if (accountsProfiles.isNotEmpty()) {
            OutlinedButton(
                onClick = { showAccountsDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (preference.accounts) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                )
            ) {
                Text("Accounts", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showAccountsDialog && accountsProfiles.isNotEmpty()) {
        AccountsQuickSettingsDialog(
            onDismissRequest = { showAccountsDialog = false },
            onConfirm = {},
            accountProfiles = accountsProfiles,
            onAccountProfileClicked = { onNavigateToAccountGeneratorPage(it.url) },
            isAccountsEnabled = preference.accounts,
            onAccountsToggled = {
                ACCOUNTS.updateBoolean(!preference.accounts)
                onPreferenceUpdate()
            },
        )
    }
}

@Composable
fun ExpandableTitle(
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(thickness = Dp.Hairline, modifier = Modifier.padding(horizontal = 20.dp))
        Column(
            modifier =
                modifier
                    .clickable(
                        onClick = onClick,
                        onClickLabel = stringResource(R.string.show_more_actions),
                        enabled = !expanded,
                    )
                    .padding(top = 12.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = stringResource(R.string.additional_settings),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!expanded) {
                    Icon(
                        imageVector = Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
            AnimatedVisibility(expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SingleChoiceItem(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    selected: Boolean,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val corner by
        animateDpAsState(
            if (selected) 28.dp else 16.dp,
            animationSpec =
                spring(
                    stiffness = Spring.StiffnessMedium,
                    visibilityThreshold = Dp.VisibilityThreshold,
                ),
            label = "",
        )
    val color by
        animateColorAsState(
            if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
            label = "",
        )

    Surface(
        selected = selected,
        onClick = onClick,
        color = color,
        shape = RoundedCornerShape(corner),
        modifier = modifier.padding(vertical = 4.dp).run { if (!enabled) alpha(0.32f) else this },
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    icon?.invoke()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 32.dp),
                )
            }
            action?.invoke()
        }
    }
}

@Composable
internal fun Header(modifier: Modifier = Modifier, icon: ImageVector, title: String) {
    Column(modifier = modifier) {
        Icon(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            imageVector = icon,
            contentDescription = null,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier =
                Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp, bottom = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DownloadTypeSelectionGroup(
    modifier: Modifier = Modifier,
    typeEntries: List<DownloadType>,
    selectedType: DownloadType?,
    onSelect: (DownloadType) -> Unit,
) {
    val typeCount = typeEntries.size
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        typeEntries.forEachIndexed { index, type ->
            SingleChoiceSegmentedButton(
                selected = selectedType == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index, typeCount),
            ) {
                Text(text = type.label())
            }
        }
    }
}

@Composable
private fun Preset(
    modifier: Modifier = Modifier,
    preference: DownloadUtil.DownloadPreferences,
    downloadType: DownloadType?,
    selected: Boolean,
    showEditIcon: Boolean,
    onEdit: () -> Unit,
    onClick: () -> Unit,
) {
    val description =
        when (downloadType) {
            Audio -> {
                PreferenceStrings.getAudioPresetText(preference)
            }

            Video -> {
                PreferenceStrings.getVideoPresetText(preference)
            }

            else -> ""
        }

    SingleChoiceItem(
        modifier = modifier,
        title = stringResource(R.string.preset),
        desc = description,
        icon = {
            Crossfade(selected, animationSpec = spring(stiffness = Spring.StiffnessMedium)) {
                if (it) {
                    Icon(
                        imageVector = Icons.Filled.SettingsSuggest,
                        null,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.SettingsSuggest,
                        null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        selected = selected,
        action = {
            Crossfade(showEditIcon, animationSpec = spring(stiffness = Spring.StiffnessMedium)) {
                if (it) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.edit),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        onClick = {
            if (showEditIcon) {
                onEdit()
            } else {
                onClick()
            }
        },
    )
}

@Composable
private fun Custom(
    modifier: Modifier = Modifier,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    SingleChoiceItem(
        modifier = modifier,
        title = stringResource(R.string.custom),
        desc = stringResource(R.string.custom_format_selection_desc),
        icon = {
            Crossfade(selected, animationSpec = spring(stiffness = Spring.StiffnessMedium)) {
                if (it) {
                    Icon(
                        imageVector = Icons.Filled.VideoFile,
                        null,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.VideoFile,
                        null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        selected = selected,
        enabled = enabled,
        onClick = onClick,
    )
}

private enum class ActionButton {
    FetchInfo,
    Download,
    StartTask,
}

@Composable
private fun ActionButton.Icon() {
    Icon(
        imageVector =
            when (this) {
                FetchInfo -> Icons.AutoMirrored.Filled.ArrowForward
                Download -> Icons.Outlined.CheckCircle
                StartTask -> Icons.Filled.DownloadDone
            },
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun ActionButton.Label() {
    Text(
        text = when (this) {
            FetchInfo -> "Proceed"
            Download -> "Download"
            StartTask -> "Start"
        },
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    canProceed: Boolean,
    selectedType: DownloadType?,
    useFormatSelection: Boolean,
    onCancel: () -> Unit,
    onFetchInfo: () -> Unit,
    onDownload: () -> Unit,
    onTaskStart: () -> Unit,
) {
    val action =
        if (selectedType == Command) {
            StartTask
        } else if (useFormatSelection) {
            FetchInfo
        } else {
            Download
        }

    val state = rememberLazyListState()
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.height(48.dp).weight(1f, fill = false),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Outlined.Cancel, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Cancel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }

        Button(
            modifier = Modifier.height(48.dp).weight(1f, fill = false),
            onClick = {
                when (action) {
                    FetchInfo -> onFetchInfo()
                    Download -> onDownload()
                    StartTask -> onTaskStart()
                }
            },
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            enabled = canProceed,
        ) {
            AnimatedContent(
                targetState = action,
                label = "",
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90))).togetherWith(
                        fadeOut(animationSpec = tween(90))
                    )
                },
            ) { action ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    action.Icon()
                    Spacer(Modifier.width(10.dp))
                    action.Label()
                }
            }
        }
    }
}
