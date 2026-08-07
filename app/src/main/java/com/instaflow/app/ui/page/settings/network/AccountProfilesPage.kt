package com.instaflow.app.ui.page.settings.network

import android.content.res.Configuration
import android.webkit.CookieManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instaflow.app.R
import com.instaflow.app.database.objects.AccountProfile
import com.instaflow.app.ui.common.HapticFeedback.slightHapticFeedback
import com.instaflow.app.ui.common.booleanState
import com.instaflow.app.ui.component.BackButton
import com.instaflow.app.ui.component.ConfirmButton
import com.instaflow.app.ui.component.DialogSwitchItem
import com.instaflow.app.ui.component.DismissButton
import com.instaflow.app.ui.component.HelpDialog
import com.instaflow.app.ui.component.PasteFromClipBoardButton
import com.instaflow.app.ui.component.PreferenceItemVariant
import com.instaflow.app.ui.component.PreferenceSwitchWithContainer
import com.instaflow.app.ui.component.InstaFlowDialog
import com.instaflow.app.ui.component.TextButtonWithIcon
import com.instaflow.app.ui.theme.InstaFlowTheme
import com.instaflow.app.ui.theme.generateLabelColor
import com.instaflow.app.util.ACCOUNTS
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.DownloadUtil.toAccountSessionFileContent
import com.instaflow.app.util.FileUtil
import com.instaflow.app.util.FileUtil.getAccountSessionFile
import com.instaflow.app.util.PreferenceUtil.getBoolean
import com.instaflow.app.util.PreferenceUtil.updateBoolean
import com.instaflow.app.util.USER_AGENT
import com.instaflow.app.util.matchUrlFromClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountProfilePage(
    accountsViewModel: AccountsViewModel,
    navigateToAccountGeneratorPage: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val accounts = accountsViewModel.accountsFlow.collectAsState(emptyList()).value
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val state by accountsViewModel.stateFlow.collectAsStateWithLifecycle()
    var showClearAccountDialog by remember { mutableStateOf(false) }
    var isAccountEnabled by remember { mutableStateOf(ACCOUNTS.getBoolean()) }
    val cookieManager = CookieManager.getInstance()
    var showHelpDialog by remember { mutableStateOf(false) }
    val view = LocalView.current

    var accountList by remember { mutableStateOf(listOf<AccountSession>()) }

    var shouldUpdateAccounts by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    DisposableEffect(shouldUpdateAccounts) {
        scope.launch(Dispatchers.IO) {
            DownloadUtil.getAccountListFromDatabase().getOrNull()?.let {
                accountList = it
                FileUtil.writeContentToFile(it.toAccountSessionFileContent(), context.getAccountSessionFile())
            }
        }
        onDispose { shouldUpdateAccounts = false }
    }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(accountList.toAccountSessionFileContent().toByteArray())
                    }
                }
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(modifier = Modifier, text = stringResource(id = R.string.accounts)) },
                navigationIcon = { BackButton { onNavigateBack() } },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.how_does_it_work),
                        )
                    }
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.show_more_actions),
                        )
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        var userAgent by USER_AGENT.booleanState
                        fun toggleUserAgent(boolean: Boolean = !userAgent) {
                            expanded = false
                            userAgent = boolean
                            USER_AGENT.updateBoolean(boolean)
                        }
                        DropdownMenuItem(
                            modifier =
                                Modifier.toggleable(
                                    value = userAgent,
                                    onValueChange = ::toggleUserAgent,
                                ),
                            leadingIcon = {
                                Checkbox(
                                    checked = userAgent,
                                    onCheckedChange = null,
                                    modifier = Modifier.clearAndSetSemantics {},
                                )
                            },
                            text = { Text(stringResource(id = R.string.ua_header)) },
                            onClick = ::toggleUserAgent,
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Outlined.FileCopy, null) },
                            text = { Text(stringResource(id = R.string.export_to_file)) },
                            enabled = accountList.isNotEmpty(),
                            onClick = {
                                expanded = false
                                exportLauncher.launch(
                                    "accounts_exported${System.currentTimeMillis()}.txt"
                                )
                            },
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Outlined.DeleteForever, null) },
                            text = { Text(stringResource(id = R.string.disconnect_all_accounts)) },
                            onClick = {
                                expanded = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showClearAccountDialog = true
                            },
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(modifier = Modifier, contentPadding = paddingValues) {
            item {
                PreferenceSwitchWithContainer(
                    title = stringResource(R.string.stay_logged_in),
                    icon = null,
                    isChecked = isAccountEnabled,
                    onClick = {
                        if (isAccountEnabled) {
                            isAccountEnabled = false
                            ACCOUNTS.updateBoolean(false)
                        } else if (
                            (accounts.isEmpty() || !cookieManager.hasCookies()) && !isAccountEnabled
                        ) {
                            showHelpDialog = true
                        } else {
                            isAccountEnabled = true
                            ACCOUNTS.updateBoolean(true)
                        }
                    },
                )
            }
            itemsIndexed(accounts) { _, item ->
                PreferenceItemVariant(
                    modifier = Modifier.padding(vertical = 4.dp),
                    title = item.url,
                    onClick = {
                        accountsViewModel.setEditingProfile(item)
                        showEditDialog = true
                    },
                    onClickLabel = stringResource(id = R.string.edit),
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        accountsViewModel.setEditingProfile(item)
                        showDeleteDialog = true
                    },
                    onLongClickLabel = stringResource(R.string.remove),
                )
            }

            item {
                PreferenceItemVariant(
                    title = "Connect Instagram Account",
                    icon = Icons.Outlined.Add,
                ) {
                    accountsViewModel.setEditingProfile(
                        com.instaflow.app.database.objects.AccountProfile(
                            id = 0,
                            url = "https://www.instagram.com/accounts/login/",
                            content = ""
                        )
                    )
                    navigateToAccountGeneratorPage()
                }
            }
            item {
                androidx.compose.material3.HorizontalDivider()
                val accountsCount = accountList.size
                val siteCount = accountList.distinctBy { it.domain }.size
                Text(
                    text = stringResource(R.string.accounts_in_database, accountsCount, siteCount),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (showEditDialog) {
        AccountGeneratorDialog(
            accountsViewModel = accountsViewModel,
            navigateToAccountGeneratorPage = {
                accountsViewModel.updateAccountProfile()
                navigateToAccountGeneratorPage()
            },
        ) {
            showEditDialog = false
            shouldUpdateAccounts = true
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(accountsViewModel) { showDeleteDialog = false }
    }

    if (showHelpDialog) {
        HelpDialog(
            text = stringResource(id = R.string.accounts_usage_msg),
            onDismissRequest = { showHelpDialog = false },
        )
    }
    if (showClearAccountDialog) {
        ClearAccountsDialog(onDismissRequest = { showClearAccountDialog = false }) {
            view.slightHapticFeedback()
            scope
                .launch(Dispatchers.IO) { CookieManager.getInstance().removeAllCookies(null) }
                .invokeOnCompletion { shouldUpdateAccounts = true }
        }
    }
}

@Composable
fun AccountGeneratorDialog(
    accountsViewModel: AccountsViewModel,
    navigateToAccountGeneratorPage: () -> Unit = {},
    onDismissRequest: () -> Unit,
) {

    val state by accountsViewModel.stateFlow.collectAsStateWithLifecycle()
    val profile = state.editingAccountProfile
    val url = profile.url

    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { CookieManager.getInstance().flush() } }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.AccountCircle, null) },
        title = { Text(stringResource(R.string.accounts)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    value = url,
                    label = { Text("URL") },
                    onValueChange = { accountsViewModel.updateUrl(it) },
                    trailingIcon = {
                        PasteFromClipBoardButton {
                            accountsViewModel.updateUrl(matchUrlFromClipboard(it))
                        }
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )

                TextButtonWithIcon(
                    onClick = { navigateToAccountGeneratorPage() },
                    icon = Icons.Outlined.GeneratingTokens,
                    text = stringResource(id = R.string.connect_account),
                )
            }
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        confirmButton = {
            ConfirmButton(enabled = url.isNotEmpty()) {
                accountsViewModel.updateAccountProfile()
                onDismissRequest()
            }
        },
    )
}

@Composable
fun DeleteAccountDialog(accountsViewModel: AccountsViewModel, onDismissRequest: () -> Unit = {}) {
    val state by accountsViewModel.stateFlow.collectAsState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.remove)) },
        text = {
            Text(
                stringResource(R.string.remove_account_session_desc)
                    .format(state.editingAccountProfile.url),
                style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph),
            )
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        confirmButton = {
            ConfirmButton {
                accountsViewModel.deleteAccountProfile()
                onDismissRequest()
            }
        },
        icon = { Icon(Icons.Outlined.Delete, null) },
    )
}

@Composable
fun ClearAccountsDialog(onDismissRequest: () -> Unit = {}, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.disconnect_all_accounts)) },
        text = {
            Text(
                stringResource(R.string.disconnect_all_accounts_desc),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        dismissButton = { DismissButton { onDismissRequest() } },
        confirmButton = {
            ConfirmButton {
                onConfirm()
                onDismissRequest()
            }
        },
        icon = { Icon(Icons.Outlined.DeleteForever, null) },
    )
}

@Composable
fun AccountsQuickSettingsDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
    accountProfiles: List<AccountProfile> = emptyList(),
    onAccountProfileClicked: (AccountProfile) -> Unit = {},
    isAccountsEnabled: Boolean = false,
    onAccountsToggled: (Boolean) -> Unit = {},
) {
    InstaFlowDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            ConfirmButton(
                text = stringResource(id = androidx.appcompat.R.string.abc_action_mode_done)
            ) {
                onDismissRequest()
                onConfirm()
            }
        },
        icon = { Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null) },
        title = {
            Text(text = stringResource(id = R.string.accounts), textAlign = TextAlign.Center)
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.refresh_accounts_desc),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    //                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn() {
                    items(items = accountProfiles) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable { onAccountProfileClicked(it) }
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier.padding(end = 12.dp)
                                        .size(16.dp)
                                        .background(
                                            color = it.url.hashCode().generateLabelColor(),
                                            shape = CircleShape,
                                        )
                                        .clearAndSetSemantics {}
                            ) {}
                            Text(
                                text = it.url
                                //                                , style =
                                // MaterialTheme.typography.labelLarge
                                ,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                DialogSwitchItem(
                    text = stringResource(id = R.string.stay_logged_in),
                    value = isAccountsEnabled,
                    onValueChange = onAccountsToggled,
                )
            }
        },
    )
}

@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AccountsQuickSettingsDialogPreview() {
    InstaFlowTheme {
        var isAccountsEnabled by remember { mutableStateOf(false) }
        AccountsQuickSettingsDialog(
            accountProfiles =
                mutableListOf<AccountProfile>().apply {
                    repeat(4) {
                        add(
                            AccountProfile(id = it, url = "https://www.example$it.com", content = "")
                        )
                    }
                },
            isAccountsEnabled = isAccountsEnabled,
            onAccountsToggled = { isAccountsEnabled = it },
        )
    }
}
