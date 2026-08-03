package com.instasave.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instasave.app.core.data.settings.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val autoPasteEnabled: StateFlow<Boolean> = settingsDataStore.autoPasteEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val defaultHighQuality: StateFlow<Boolean> = settingsDataStore.defaultHighQuality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleAutoPaste(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAutoPasteEnabled(enabled)
        }
    }

    fun toggleDefaultHighQuality(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDefaultHighQuality(enabled)
        }
    }
}
