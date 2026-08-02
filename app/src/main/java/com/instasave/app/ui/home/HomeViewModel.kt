package com.instasave.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasave.app.core.network.generated.api.InstaSaveApi
import com.instasave.app.core.network.generated.model.ResolveRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: InstaSaveApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.UrlChanged -> _uiState.update { it.copy(urlInput = event.newUrl) }
            is HomeUiEvent.PasteFromClipboard -> { /* Handled at Screen layer via ClipboardManager */ }
            is HomeUiEvent.ClearUrl -> _uiState.update { it.copy(urlInput = "") }
            is HomeUiEvent.ResolveClicked -> resolveCurrentUrl()
            is HomeUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
            is HomeUiEvent.ClearResolvedMedia -> _uiState.update { it.copy(resolvedMedia = null) }
        }
    }

    fun handleSharedUrl(url: String) {
        _uiState.update { it.copy(urlInput = url) }
        resolveCurrentUrl()
    }

    private fun resolveCurrentUrl() {
        val currentUrl = _uiState.value.urlInput.trim()
        if (currentUrl.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter or paste an Instagram URL") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isResolving = true, error = null) }
            try {
                val mediaInfo = api.resolveMedia(ResolveRequest(url = currentUrl))
                _uiState.update { it.copy(isResolving = false, resolvedMedia = mediaInfo) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isResolving = false,
                        error = e.localizedMessage ?: "Failed to resolve Instagram media link"
                    )
                }
            }
        }
    }
}
