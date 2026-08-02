package com.instasave.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing type-safe screens and navigation targets.
 */
sealed class ScreenRoute(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : ScreenRoute("home", "Home", Icons.Default.Home)
    object Downloads : ScreenRoute("downloads", "Downloads", Icons.Default.Download)
    object History : ScreenRoute("history", "History", Icons.Default.History)
    object Settings : ScreenRoute("settings", "Settings", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Downloads, History, Settings)
    }
}
