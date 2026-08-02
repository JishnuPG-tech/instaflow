package com.instasave.app.ui.home

import com.instasave.app.core.network.generated.model.MediaInfo

data class HomeUiState(
    val urlInput: String = "",
    val isResolving: Boolean = false,
    val resolvedMedia: MediaInfo? = null,
    val error: String? = null
)

sealed interface HomeUiEvent {
    data class UrlChanged(val newUrl: String) : HomeUiEvent
    data object PasteFromClipboard : HomeUiEvent
    data object ClearUrl : HomeUiEvent
    data object ResolveClicked : HomeUiEvent
    data object DismissError : HomeUiEvent
    data object ClearResolvedMedia : HomeUiEvent
}
