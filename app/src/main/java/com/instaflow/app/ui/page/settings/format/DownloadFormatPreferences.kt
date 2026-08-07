package com.instaflow.app.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArtTrack
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.instaflow.app.R
import com.instaflow.app.ui.common.booleanState
import com.instaflow.app.ui.common.intState
import com.instaflow.app.ui.component.BackButton
import com.instaflow.app.ui.component.PreferenceInfo
import com.instaflow.app.ui.component.PreferenceItem
import com.instaflow.app.ui.component.PreferenceSubtitle
import com.instaflow.app.ui.component.PreferenceSwitch
import com.instaflow.app.ui.component.PreferenceSwitchWithDivider
import com.instaflow.app.util.AUDIO_CONVERSION_FORMAT
import com.instaflow.app.util.AUDIO_CONVERT
import com.instaflow.app.util.CROP_ARTWORK
import com.instaflow.app.util.CUSTOM_COMMAND
import com.instaflow.app.util.EMBED_METADATA
import com.instaflow.app.util.EXTRACT_AUDIO
import com.instaflow.app.util.PreferenceStrings
import com.instaflow.app.util.PreferenceUtil
import com.instaflow.app.util.PreferenceUtil.getBoolean
import com.instaflow.app.util.PreferenceUtil.updateBoolean
import com.instaflow.app.util.PreferenceUtil.updateInt
import com.instaflow.app.util.VIDEO_FORMAT
import com.instaflow.app.util.VIDEO_QUALITY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadFormatPreferences(onNavigateBack: () -> Unit, navigateToSubtitlePage: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )

    var audioSwitch by remember { mutableStateOf(EXTRACT_AUDIO.getBoolean()) }
    var isArtworkCroppingEnabled by remember { mutableStateOf(CROP_ARTWORK.getBoolean()) }
    var embedMetadata by EMBED_METADATA.booleanState

    var showAudioConvertDialog by remember { mutableStateOf(false) }
    var showVideoQualityDialog by remember { mutableStateOf(false) }
    var showVideoFormatDialog by remember { mutableStateOf(false) }

    var videoFormat by VIDEO_FORMAT.intState
    var videoQuality by VIDEO_QUALITY.intState
    var convertFormat by AUDIO_CONVERSION_FORMAT.intState
    var convertAudio by AUDIO_CONVERT.booleanState

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(modifier = Modifier, text = stringResource(id = R.string.format)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            val isCustomCommandEnabled by remember { mutableStateOf(CUSTOM_COMMAND.getBoolean()) }
            LazyColumn(contentPadding = it) {
                if (isCustomCommandEnabled)
                    item {
                        PreferenceInfo(
                            text = stringResource(id = R.string.custom_command_enabled_hint)
                        )
                    }
                item { PreferenceSubtitle(text = stringResource(id = R.string.audio)) }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.extract_audio),
                        description = stringResource(id = R.string.extract_audio_summary),
                        icon = Icons.Outlined.MusicNote,
                        isChecked = audioSwitch,
                        enabled = !isCustomCommandEnabled,
                        onClick = {
                            audioSwitch = !audioSwitch
                            PreferenceUtil.updateValue(EXTRACT_AUDIO, audioSwitch)
                        },
                    )
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.convert_audio_format),
                        description = PreferenceStrings.getAudioConvertDesc(convertFormat),
                        icon = Icons.Outlined.Sync,
                        enabled = audioSwitch && !isCustomCommandEnabled,
                        onClick = { showAudioConvertDialog = true },
                        isChecked = convertAudio,
                        onChecked = {
                            convertAudio = !convertAudio
                            AUDIO_CONVERT.updateBoolean(convertAudio)
                        },
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.embed_metadata),
                        description = stringResource(id = R.string.embed_metadata_desc),
                        enabled = audioSwitch && !isCustomCommandEnabled,
                        isChecked = embedMetadata,
                        icon = Icons.Outlined.ArtTrack,
                        onClick = {
                            embedMetadata = !embedMetadata
                            EMBED_METADATA.updateBoolean(embedMetadata)
                        },
                    )
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.crop_artwork),
                        description = stringResource(R.string.crop_artwork_desc),
                        icon = Icons.Outlined.Crop,
                        enabled = embedMetadata && audioSwitch && !isCustomCommandEnabled,
                        isChecked = isArtworkCroppingEnabled,
                    ) {
                        isArtworkCroppingEnabled = !isArtworkCroppingEnabled
                        PreferenceUtil.updateValue(CROP_ARTWORK, isArtworkCroppingEnabled)
                    }
                }
                item { PreferenceSubtitle(text = stringResource(id = R.string.video)) }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.video_format_preference),
                        description = PreferenceStrings.getVideoFormatLabel(videoFormat),
                        icon = Icons.Outlined.VideoFile,
                        enabled = !audioSwitch && !isCustomCommandEnabled,
                    ) {
                        showVideoFormatDialog = true
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_quality),
                        description = PreferenceStrings.getVideoResolutionDesc(videoQuality),
                        icon = Icons.Outlined.HighQuality,
                        enabled = !audioSwitch && !isCustomCommandEnabled,
                    ) {
                        showVideoQualityDialog = true
                    }
                }

                item { PreferenceSubtitle(text = stringResource(id = R.string.advanced_settings)) }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.subtitle),
                        icon = Icons.Outlined.Subtitles,
                        enabled = !isCustomCommandEnabled,
                        description = stringResource(id = R.string.subtitle_desc),
                    ) {
                        navigateToSubtitlePage()
                    }
                }
            }
        },
    )
    if (showAudioConvertDialog) {
        AudioConversionDialog(
            onDismissRequest = { showAudioConvertDialog = false },
            audioFormat = convertFormat,
            onConfirm = {
                convertFormat = it
                AUDIO_CONVERSION_FORMAT.updateInt(it)
            },
        )
    }
    if (showVideoQualityDialog) {
        VideoQualityDialog(
            videoQuality = videoQuality,
            onDismissRequest = { showVideoQualityDialog = false },
        ) {
            videoQuality = it
            VIDEO_QUALITY.updateInt(it)
        }
    }
    if (showVideoFormatDialog) {
        VideoFormatDialog(
            videoFormatPreference = videoFormat,
            onDismissRequest = { showVideoFormatDialog = false },
        ) {
            PreferenceUtil.encodeInt(VIDEO_FORMAT, it)
            videoFormat = it
        }
    }
}
