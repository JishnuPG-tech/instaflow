package com.instasave.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasave.app.core.download.DownloadQueueManager
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
    private val api: InstaSaveApi,
    private val downloadQueueManager: DownloadQueueManager
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
            is HomeUiEvent.ClearResolvedMedia -> _uiState.update { it.copy(resolvedMedia = null, selectedFormat = null) }
            is HomeUiEvent.ToggleCarouselItem -> {
                _uiState.update { state ->
                    val newSet = state.selectedCarouselIndices.toMutableSet()
                    if (newSet.contains(event.index)) {
                        newSet.remove(event.index)
                    } else {
                        newSet.add(event.index)
                    }
                    state.copy(selectedCarouselIndices = newSet)
                }
            }
            is HomeUiEvent.FormatSelected -> {
                _uiState.update { it.copy(selectedFormat = event.format) }
                triggerDownload(event.format)
            }
        }
    }

    fun handleSharedUrl(url: String) {
        _uiState.update { it.copy(urlInput = url) }
        resolveCurrentUrl()
    }

    private fun triggerDownload(format: com.instasave.app.core.network.generated.model.MediaFormat) {
        val media = _uiState.value.resolvedMedia ?: return
        val isVideo = media.type == "video" || media.type == "reel"
        val fileName = "InstaSave_${System.currentTimeMillis()}.${format.ext}"
        
        downloadQueueManager.enqueueDownload(
            url = format.url,
            fileName = fileName,
            mimeType = if (isVideo) "video/mp4" else "image/jpeg",
            isVideo = isVideo
        )

        // Dismiss sheet
        _uiState.update { it.copy(resolvedMedia = null, selectedFormat = null) }
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
                val initialIndices = mediaInfo.carouselItems?.indices?.toSet() ?: emptySet()
                _uiState.update {
                    it.copy(
                        isResolving = false,
                        resolvedMedia = mediaInfo,
                        selectedCarouselIndices = initialIndices
                    )
                }
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
