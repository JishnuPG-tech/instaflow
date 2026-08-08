package com.instaflow.app.ui.page.downloadv2

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instaflow.app.R
import com.instaflow.app.download.DownloaderV2
import com.instaflow.app.download.Task
import com.instaflow.app.download.Task.DownloadState.Canceled
import com.instaflow.app.download.Task.DownloadState.Completed
import com.instaflow.app.download.Task.DownloadState.Error
import com.instaflow.app.download.Task.DownloadState.FetchingInfo
import com.instaflow.app.download.Task.DownloadState.Idle
import com.instaflow.app.download.Task.DownloadState.ReadyWithInfo
import com.instaflow.app.download.Task.DownloadState.Running
import com.instaflow.app.ui.common.HapticFeedback.slightHapticFeedback
import com.instaflow.app.ui.common.LocalDarkTheme
import com.instaflow.app.ui.common.LocalFixedColorRoles
import com.instaflow.app.ui.common.LocalWindowWidthState
import com.instaflow.app.ui.component.InstaFlowModalBottomSheet
import com.instaflow.app.ui.component.SelectionGroupDefaults
import com.instaflow.app.ui.component.SelectionGroupItem
import com.instaflow.app.ui.component.SelectionGroupRow
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.instaflow.app.ui.page.downloadv2.configure.Config
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialog
import com.instaflow.app.ui.page.downloadv2.configure.DownloadDialogViewModel
import com.instaflow.app.ui.page.downloadv2.configure.FormatPage
import com.instaflow.app.ui.page.downloadv2.configure.PlaylistSelectionPage
import com.instaflow.app.ui.page.downloadv2.configure.PreferencesMock
import com.instaflow.app.ui.svg.DynamicColorImageVectors
import com.instaflow.app.ui.svg.drawablevectors.download
import com.instaflow.app.ui.theme.InstaFlowTheme
import com.instaflow.app.ui.page.settings.network.AccountsViewModel
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.FileUtil
import com.instaflow.app.util.getErrorReport
import com.instaflow.app.util.makeToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val TAG = "DownloadPageV2"

enum class Filter {
    All,
    Downloading,
    Canceled,
    Finished;

    @Composable
    @ReadOnlyComposable
    fun label(): String =
        when (this) {
            All -> stringResource(R.string.all)
            Downloading -> stringResource(R.string.status_downloading)
            Canceled -> stringResource(R.string.status_canceled)
            Finished -> stringResource(R.string.status_completed)
        }

    fun predict(entry: Pair<Task, Task.State>): Boolean {
        if (this == All) return true
        val state = entry.second.downloadState
        return when (this) {
            Downloading -> {
                when (state) {
                    is FetchingInfo,
                    Idle,
                    ReadyWithInfo,
                    is Running -> true
                    else -> false
                }
            }
            Canceled -> {
                state is Error || state is Task.DownloadState.Canceled
            }
            Finished -> {
                state is Completed
            }
            else -> {
                true
            }
        }
    }
}

sealed interface UiAction {
    data class OpenFile(val filePath: String?) : UiAction

    data class ShareFile(val filePath: String?) : UiAction

    data class OpenThumbnailURL(val url: String) : UiAction

    data object CopyVideoURL : UiAction

    data class OpenVideoURL(val url: String) : UiAction

    data object Cancel : UiAction

    data object Delete : UiAction

    data object Resume : UiAction

    data class CopyErrorReport(val throwable: Throwable) : UiAction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageV2(
    modifier: Modifier = Modifier,
    onMenuOpen: (() -> Unit) = {},
    onNavigateToAccountProfile: () -> Unit = {},
    dialogViewModel: DownloadDialogViewModel,
    accountsViewModel: AccountsViewModel = koinViewModel(),
    downloader: DownloaderV2 = koinInject(),
) {
    val view = LocalView.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val accountState by accountsViewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        accountsViewModel.checkInstagramConnection()
    }

    DownloadPageImplV2(
        modifier = modifier,
        taskDownloadStateMap = downloader.getTaskStateMap(),
        isAccountConnected = accountState.isInstagramConnected,
        onConnectAccount = {
            view.slightHapticFeedback()
            onNavigateToAccountProfile()
        },
        downloadCallback = {
            view.slightHapticFeedback()
            dialogViewModel.postAction(Action.ShowSheet())
        },
        onMenuOpen = onMenuOpen,
    ) { task, action ->
        view.slightHapticFeedback()
        when (action) {
            UiAction.Cancel -> downloader.cancel(task)
            UiAction.Delete -> downloader.remove(task)
            UiAction.Resume -> downloader.restart(task)
            is UiAction.CopyErrorReport -> {
                clipboardManager.setText(
                    AnnotatedString(getErrorReport(action.throwable, task.url))
                )
                context.makeToast(R.string.error_copied)
            }
            UiAction.CopyVideoURL -> {
                clipboardManager.setText(AnnotatedString(task.url))
                context.makeToast(R.string.link_copied)
            }
            is UiAction.OpenFile -> {
                action.filePath?.let {
                    FileUtil.openFile(path = it) { context.makeToast(R.string.file_unavailable) }
                }
            }
            is UiAction.OpenThumbnailURL -> {
                uriHandler.openUri(action.url)
            }
            is UiAction.OpenVideoURL -> {
                uriHandler.openUri(action.url)
            }
            is UiAction.ShareFile -> {
                val shareTitle = context.getString(R.string.share)
                FileUtil.createIntentForSharingFile(action.filePath)?.let {
                    context.startActivity(Intent.createChooser(it, shareTitle))
                }
            }
        }
    }

    var preferences by remember {
        mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
    }
    val sheetValue by dialogViewModel.sheetValueFlow.collectAsStateWithLifecycle()
    val state by dialogViewModel.sheetStateFlow.collectAsStateWithLifecycle()

    val selectionState = dialogViewModel.selectionStateFlow.collectAsStateWithLifecycle().value

    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(sheetValue) {
        if (sheetValue == DownloadDialogViewModel.SheetValue.Expanded) {
            showDialog = true
        } else {
            launch { sheetState.hide() }.invokeOnCompletion { showDialog = false }
        }
    }

    if (showDialog) {

        DownloadDialog(
            state = state,
            sheetState = sheetState,
            config = Config(),
            preferences = preferences,
            onPreferencesUpdate = { preferences = it },
            onActionPost = { dialogViewModel.postAction(it) },
        )
    }
    when (selectionState) {
        is DownloadDialogViewModel.SelectionState.FormatSelection ->
            FormatPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )

        is DownloadDialogViewModel.SelectionState.PlaylistSelection -> {
            PlaylistSelectionPage(
                state = selectionState,
                onDismissRequest = { dialogViewModel.postAction(Action.Reset) },
            )
        }

        DownloadDialogViewModel.SelectionState.Idle -> {}
    }
}

@Composable
private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        top = calculateTopPadding() + other.calculateTopPadding(),
        bottom = calculateBottomPadding() + other.calculateBottomPadding(),
        start =
            calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
    )
}

private const val HeaderSpacingDp = 28

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadPageImplV2(
    modifier: Modifier = Modifier,
    taskDownloadStateMap: SnapshotStateMap<Task, Task.State>,
    isAccountConnected: Boolean = false,
    onConnectAccount: () -> Unit = {},
    downloadCallback: () -> Unit = {},
    onMenuOpen: (() -> Unit) = {},
    onActionPost: (Task, UiAction) -> Unit,
) {
    var activeFilter by remember { mutableStateOf(Filter.All) }
    val filteredMap by
        remember(activeFilter) {
            derivedStateOf { taskDownloadStateMap.filter { activeFilter.predict(it.toPair()) } }
        }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val view = LocalView.current

    fun showActionSheet(task: Task) {
        view.slightHapticFeedback()
        scope.launch {
            selectedTask = task
            delay(50)
            sheetState.show()
        }
    }

    LaunchedEffect(selectedTask, taskDownloadStateMap.size) {
        if (!taskDownloadStateMap.contains(selectedTask)) {
            selectedTask == null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = { FABs(modifier = Modifier, downloadCallback = downloadCallback) },
    ) { windowInsetsPadding ->
        val lazyListState = rememberLazyStaggeredGridState()
        val windowWidthSizeClass = LocalWindowWidthState.current
        val spacerHeight =
            with(LocalDensity.current) {
                if (windowWidthSizeClass != WindowWidthSizeClass.Compact) 0f
                else HeaderSpacingDp.dp.toPx()
            }
        var headerOffset by remember { mutableFloatStateOf(spacerHeight) }
        var isGridView by rememberSaveable { mutableStateOf(true) }

        Column(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (windowWidthSizeClass != WindowWidthSizeClass.Compact) Modifier
                        else
                            Modifier.nestedScroll(
                                connection =
                                    TopBarNestedScrollConnection(
                                        maxOffset = spacerHeight,
                                        flingAnimationSpec = rememberSplineBasedDecay(),
                                        offset = { headerOffset },
                                        onOffsetUpdate = { headerOffset = it },
                                    )
                            )
                    )
        ) {
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(with(LocalDensity.current) { headerOffset.toDp() }))
                    Header(
                        onMenuOpen = onMenuOpen, 
                        modifier = Modifier.padding(horizontal = 16.dp),
                        isAccountConnected = isAccountConnected,
                        onConnectAccount = onConnectAccount
                    )
                    SelectionGroupRow(
                        modifier =
                            Modifier.horizontalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                    ) {
                        Filter.entries.forEach { filter ->
                            SelectionGroupItem(
                                colors =
                                    SelectionGroupDefaults.colors(
                                        activeContainerColor =
                                            LocalFixedColorRoles.current.tertiaryFixed,
                                        activeContentColor =
                                            LocalFixedColorRoles.current.onTertiaryFixed,
                                    ),
                                selected = activeFilter == filter,
                                onClick = {
                                    if (activeFilter == filter) {
                                        scope.launch { lazyListState.animateScrollToItem(0) }
                                        scope.launch {
                                            val initialValue = headerOffset
                                            AnimationState(initialValue = initialValue).animateTo(
                                                spacerHeight
                                            ) {
                                                headerOffset = value
                                            }
                                        }
                                    } else {
                                        activeFilter = filter
                                    }
                                },
                            ) {
                                Text(filter.label())
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (headerOffset <= 0.1f && spacerHeight > 0f) {
                        HorizontalDivider(thickness = Dp.Hairline)
                    }
                }

                LazyVerticalStaggeredGrid(
                    modifier = Modifier,
                    state = lazyListState,
                    columns = StaggeredGridCells.Adaptive(180.dp),
                    contentPadding =
                        windowInsetsPadding +
                            PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalItemSpacing = 16.dp,
                ) {
                    if (filteredMap.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            val videoCount =
                                filteredMap.count {
                                    !it.value.viewState.videoFormats.isNullOrEmpty()
                                }
                            SubHeader(
                                modifier = Modifier,
                                videoCount = videoCount,
                                audioCount = filteredMap.size - videoCount,
                                isGridView = isGridView,
                                onToggleView = { isGridView = !isGridView },
                                onShowMenu = { context.makeToast("Not implemented yet!") },
                            )
                        }
                    }

                    if (isGridView) {
                        items(
                            items =
                                filteredMap.toList().sortedBy { (_, state) -> state.downloadState },
                            key = { entry -> entry.first.id },
                        ) { (task, state) ->
                            VideoCardV2(
                                modifier = Modifier.fillMaxWidth(),
                                viewState = state.viewState,
                                downloadState = state.downloadState,
                                onActionPost = { onActionPost(task, it) },
                                onButtonClick = { showActionSheet(task) },
                            )
                        }
                    } else {
                        items(
                            items =
                                filteredMap.toList().sortedBy { (_, state) -> state.downloadState },
                            key = { entry -> entry.first.id },
                            span = { StaggeredGridItemSpan.FullLine },
                        ) { (task, state) ->
                            VideoListItem(
                                modifier = Modifier.padding(bottom = 16.dp),
                                viewState = state.viewState,
                                stateIndicator = {
                                    ListItemStateText(
                                        modifier = Modifier.padding(top = 3.dp),
                                        downloadState = state.downloadState,
                                    )
                                },
                                onButtonClick = { showActionSheet(task) },
                            )
                        }
                    }
                }
            }
        }
        if (filteredMap.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                DownloadQueuePlaceholder(
                    modifier =
                        Modifier.fillMaxHeight(0.4f).widthIn(max = 360.dp).align(Alignment.Center)
                )
            }
        }
    }
    if (selectedTask != null) {
        val task = selectedTask!!
        val (downloadState, _, viewState) = taskDownloadStateMap[task] ?: return
        InstaFlowModalBottomSheet(
            sheetState = sheetState,
            contentPadding = PaddingValues(),
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTask = null }
            },
        ) {
            SheetContent(
                task = task,
                downloadState = downloadState,
                viewState = viewState,
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTask = null }
                },
                onActionPost = onActionPost,
            )
        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier, onMenuOpen: () -> Unit = {}, isAccountConnected: Boolean, onConnectAccount: () -> Unit) {
    val windowWidthSizeClass = LocalWindowWidthState.current
    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Expanded -> {
            HeaderExpanded(modifier = modifier, isAccountConnected = isAccountConnected, onConnectAccount = onConnectAccount)
        }
        else -> {
            HeaderCompact(modifier = modifier, onMenuOpen = onMenuOpen, isAccountConnected = isAccountConnected, onConnectAccount = onConnectAccount)
        }
    }
}

@Composable
private fun HeaderCompact(modifier: Modifier = Modifier, onMenuOpen: () -> Unit, isAccountConnected: Boolean, onConnectAccount: () -> Unit) {
    val view = LocalView.current
    Row(modifier = modifier.height(64.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMenuOpen, modifier = Modifier) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = stringResource(R.string.show_navigation_drawer),
                modifier = Modifier,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            stringResource(R.string.download_queue),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { 
            view.slightHapticFeedback()
            onConnectAccount() 
        }) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = stringResource(R.string.instagram_account),
                tint = if (isAccountConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeaderExpanded(modifier: Modifier = Modifier, isAccountConnected: Boolean, onConnectAccount: () -> Unit) {
    val view = LocalView.current
    Row(modifier = modifier.height(64.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            stringResource(R.string.download_queue),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { 
            view.slightHapticFeedback()
            onConnectAccount() 
        }) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = stringResource(R.string.instagram_account),
                tint = if (isAccountConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun FABs(modifier: Modifier = Modifier, downloadCallback: () -> Unit = {}) {
    val clipboardManager = LocalClipboardManager.current
    var hasIgLink by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "fab_scale"
    )

    // Simple heuristic to check if we should show "Paste & Analyze"
    LaunchedEffect(Unit) {
        delay(500) // Small delay to avoid flickering on start
        val text = clipboardManager.getText()?.text ?: ""
        hasIgLink = text.contains("instagram.com/") || text.contains("instagr.am/")
    }

    Column(modifier = modifier.padding(6.dp), horizontalAlignment = Alignment.End) {
        ExtendedFloatingActionButton(
            onClick = downloadCallback,
            interactionSource = interactionSource,
            icon = {
                Icon(
                    if (hasIgLink) Icons.Outlined.ContentPaste else Icons.Outlined.FileDownload,
                    contentDescription = null
                )
            },
            text = {
                Text(
                    if (hasIgLink) "Paste & Analyze" else stringResource(R.string.download)
                )
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(vertical = 12.dp).graphicsLayer(scaleX = scale, scaleY = scale),
        )
    }
}

@Composable
@Preview
private fun DownloadQueuePlaceholder(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        ConstraintLayout {
            val (image, text) = createRefs()
            val showImage =
                with(LocalDensity.current) {
                    this@BoxWithConstraints.constraints.maxHeight >= 240.dp.toPx()
                }
            if (showImage) {
                Image(
                    painter = rememberVectorPainter(image = DynamicColorImageVectors.download()),
                    contentDescription = null,
                    modifier =
                        Modifier.fillMaxHeight(0.5f).widthIn(max = 240.dp).constrainAs(image) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                )
            } else {
                Spacer(Modifier.height(72.dp).constrainAs(image) { top.linkTo(parent.top) })
            }
            Column(
                modifier = Modifier.constrainAs(text) { top.linkTo(image.bottom, margin = 36.dp) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.you_ll_find_your_downloads_here),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.download_hint),
                    modifier = Modifier.padding(top = 4.dp).padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun SubHeader(
    modifier: Modifier = Modifier,
    containerColor: Color =
        MaterialTheme.colorScheme.run {
            if (LocalDarkTheme.current.isDarkTheme()) surfaceContainer else surfaceContainerLowest
        },
    videoCount: Int = 0,
    audioCount: Int = 0,
    isGridView: Boolean = true,
    onToggleView: () -> Unit,
    onShowMenu: () -> Unit,
) {
    val totalCount = videoCount + audioCount
    val text = if (totalCount == 1) "1 item" else "$totalCount items"

    Row(
        modifier = modifier.padding(top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(4.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        FilledIconButton(
            onClick = onToggleView,
            modifier = Modifier.clearAndSetSemantics {}.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor),
        ) {
            Icon(
                imageVector =
                    if (isGridView) Icons.AutoMirrored.Outlined.List else Icons.Outlined.GridView,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(Modifier.width(4.dp))

        FilledIconButton(
            onClick = onShowMenu,
            modifier = Modifier.clearAndSetSemantics {}.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor),
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

internal class DownloadPageV2Test {
    private val mockDownloader =
        object : DownloaderV2 {
            private val map = mutableStateMapOf<Task, Task.State>()

            init {
                val viewState =
                    Task.ViewState(title = "Sample title", uploader = "dummy video uploader")
                val list =
                    listOf(
                        Task.State(Idle, null, viewState),
                        Task.State(Canceled(Task.RestartableAction.Download), null, viewState),
                        Task.State(Completed(null), null, viewState),
                    )
                map.run {
                    repeat(9) {
                        put(Task(url = "$it", preferences = PreferencesMock), list[it % 3])
                    }
                }
                val scope = CoroutineScope(SupervisorJob())

                scope.launch(Dispatchers.Default) {
                    while (true) {
                        delay(1000)
                        val newEntries =
                            map.toMap().map { (task, state) ->
                                val newDownloadState =
                                    when (state.downloadState) {
                                        is Canceled -> Idle
                                        is Completed -> Idle
                                        is Error -> Idle
                                        is FetchingInfo -> ReadyWithInfo
                                        Idle -> FetchingInfo(Job(), task.id)
                                        ReadyWithInfo -> Running(Job(), task.id)
                                        is Running -> {
                                            val preState: Running = state.downloadState
                                            if (preState.progress >= 1f) Completed(null)
                                            else preState.copy(progress = preState.progress + 0.1f)
                                        }
                                    }
                                task to state.copy(downloadState = newDownloadState)
                            }
                        Snapshot.withMutableSnapshot {
                            newEntries.forEach { (task, state) ->
                                delay(100)
                                map[task] = state
                            }
                        }
                    }
                }
            }

            override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
                return map
            }

            override fun cancel(task: Task): Boolean {
                return false
            }

            override fun restart(task: Task) {}

            override fun enqueue(task: Task) {}

            override fun enqueue(task: Task, state: Task.State) {}

            override fun remove(task: Task): Boolean {
                return true
            }
        }

    @Composable
    @Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Preview(name = "Tablet", device = "spec:width=600dp,height=800dp,dpi=240")
    private fun Preview() {

        val downloader: DownloaderV2 = mockDownloader
        InstaFlowTheme {
            Column() {
                DownloadPageImplV2(
                    taskDownloadStateMap = downloader.getTaskStateMap(),
                    onActionPost = { task, state -> },
                    onMenuOpen = {},
                )
            }
        }
    }
}
