package com.instasave.app.core.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val AUTO_PASTE_KEY = booleanPreferencesKey("auto_paste_clipboard")
    private val HIGH_QUALITY_DEFAULT = booleanPreferencesKey("default_high_quality")

    val autoPasteEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_PASTE_KEY] ?: true
    }

    val defaultHighQuality: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HIGH_QUALITY_DEFAULT] ?: true
    }

    suspend fun setAutoPasteEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_PASTE_KEY] = enabled
        }
    }

    suspend fun setDefaultHighQuality(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIGH_QUALITY_DEFAULT] = enabled
        }
    }
}
