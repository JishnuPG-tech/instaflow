package com.instaflow.app.ui.page.settings.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instaflow.app.App.Companion.context
import com.instaflow.app.database.objects.AccountProfile
import com.instaflow.app.util.DatabaseUtil
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.DownloadUtil.toAccountSessionFileContent
import com.instaflow.app.util.FileUtil
import com.instaflow.app.util.FileUtil.getAccountSessionFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountsViewModel : ViewModel() {
    companion object {
        const val NEW_PROFILE_ID = 0
    }

    data class ViewState(
        val editingAccountProfile: AccountProfile =
            AccountProfile(id = NEW_PROFILE_ID, url = "https://www.instagram.com/accounts/login/", content = ""),
        val isInstagramConnected: Boolean = false
    )

    val accountsFlow = DatabaseUtil.getAccountsFlow()

    private val mutableStateFlow = MutableStateFlow(ViewState())
    val stateFlow = mutableStateFlow.asStateFlow()
    private val state
        get() = stateFlow.value

    init {
        checkInstagramConnection()
    }

    fun checkInstagramConnection() {
        val hasCookies = android.webkit.CookieManager.getInstance().hasCookies()
        mutableStateFlow.update { it.copy(isInstagramConnected = hasCookies) }
    }

    fun setEditingProfile(
        accountProfile: AccountProfile =
            AccountProfile(id = NEW_PROFILE_ID, url = "https://www.instagram.com/accounts/login/", content = "")
    ) {
        val formattedUrl = when {
            accountProfile.url.isBlank() -> "https://www.instagram.com/accounts/login/"
            !accountProfile.url.startsWith("http://") && !accountProfile.url.startsWith("https://") -> "https://${accountProfile.url}"
            else -> accountProfile.url
        }
        mutableStateFlow.update { it.copy(editingAccountProfile = accountProfile.copy(url = formattedUrl)) }
    }

    fun deleteAccountProfile(accountProfile: AccountProfile = state.editingAccountProfile) {
        viewModelScope.launch(Dispatchers.IO) { 
            DatabaseUtil.deleteAccountProfile(accountProfile)
            refreshAccountSessionFile()
            checkInstagramConnection()
        }
    }

    fun generateNewAccounts(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            mutableStateFlow.update {
                val newProfile = it.editingAccountProfile.copy(content = content)
                DatabaseUtil.updateAccountProfile(newProfile)
                it.copy(editingAccountProfile = newProfile)
            }
            refreshAccountSessionFile()
        }
    }

    fun updateUrl(url: String) {
        val formattedUrl = when {
            url.isBlank() -> "https://www.instagram.com/accounts/login/"
            !url.startsWith("http://") && !url.startsWith("https://") -> "https://$url"
            else -> url
        }
        setEditingProfile(accountProfile = state.editingAccountProfile.copy(url = formattedUrl))
    }

    fun updateContent(content: String) =
        mutableStateFlow.update {
            it.copy(editingAccountProfile = it.editingAccountProfile.copy(content = content))
        }

    fun updateAccountProfile(profile: AccountProfile = state.editingAccountProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (profile.id == NEW_PROFILE_ID) {
                DatabaseUtil.insertAccountProfile(profile)
            } else {
                DatabaseUtil.updateAccountProfile(profile)
            }
            refreshAccountSessionFile()
            checkInstagramConnection()
        }
    }

    private fun refreshAccountSessionFile() {
        val synced = DownloadUtil.syncWebViewCookiesToFile()
        if (!synced) {
            DownloadUtil.getAccountListFromDatabase().getOrNull()?.let {
                FileUtil.writeContentToFile(it.toAccountSessionFileContent(), context.getAccountSessionFile())
            }
        }
    }
}
