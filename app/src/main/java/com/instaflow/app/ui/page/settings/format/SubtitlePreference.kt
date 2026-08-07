package com.instaflow.app.ui.page.settings.format

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Sync
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
import com.instaflow.app.ui.component.BackButton
import com.instaflow.app.ui.component.PreferenceItem
import com.instaflow.app.ui.component.PreferenceSwitch
import com.instaflow.app.ui.component.PreferenceSwitchWithContainer
import com.instaflow.app.util.AUTO_SUBTITLE
import com.instaflow.app.util.PreferenceStrings
import com.instaflow.app.util.PreferenceUtil.getString
import com.instaflow.app.util.PreferenceUtil.updateBoolean
import com.instaflow.app.util.SUBTITLE
import com.instaflow.app.util.SUBTITLE_LANGUAGE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitlePreference(onNavigateBack: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    var downloadSubtitle by SUBTITLE.booleanState
    var autoSubtitle by AUTO_SUBTITLE.booleanState

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showConversionDialog by remember { mutableStateOf(false) }

    val subtitleFormatText by
        remember(showConversionDialog) {
            mutableStateOf(PreferenceStrings.getSubtitleConversionFormat())
        }

    val subtitleLang by
        remember(showLanguageDialog) { mutableStateOf(SUBTITLE_LANGUAGE.getString()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(modifier = Modifier, text = stringResource(id = R.string.subtitle))
                },
                navigationIcon = { BackButton { onNavigateBack() } },
                scrollBehavior = scrollBehavior,
            )
        },
        content = {
            LazyColumn(modifier = Modifier, contentPadding = it) {
                item {
                    PreferenceSwitchWithContainer(
                        title = stringResource(id = R.string.download_subtitles),
                        isChecked = downloadSubtitle,
                        onClick = {
                            downloadSubtitle = !downloadSubtitle
                            SUBTITLE.updateBoolean(downloadSubtitle)
                        },
                        icon = null,
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.subtitle_language),
                        icon = Icons.Outlined.Language,
                        description = subtitleLang,
                        onClick = { showLanguageDialog = true },
                    )
                }

                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.convert_subtitle),
                        description = subtitleFormatText,
                        icon = Icons.Outlined.Sync,
                    ) {
                        showConversionDialog = true
                    }
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.auto_subtitle),
                        icon = Icons.Outlined.ClosedCaption,
                        description = stringResource(id = R.string.auto_subtitle_desc),
                        isChecked = autoSubtitle,
                        onClick = {
                            autoSubtitle = !autoSubtitle
                            AUTO_SUBTITLE.updateBoolean(autoSubtitle)
                        },
                    )
                }
            }
        },
    )
    if (showLanguageDialog) SubtitleLanguageDialog { showLanguageDialog = false }
    if (showConversionDialog) SubtitleConversionDialog { showConversionDialog = false }
}
