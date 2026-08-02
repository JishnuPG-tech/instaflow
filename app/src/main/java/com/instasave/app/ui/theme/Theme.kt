package com.instasave.app.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InstaSaveDarkColorScheme = darkColorScheme(
    background = BgBase,
    surface = BgSurface,
    surfaceVariant = BgSurfaceHigh,
    primary = AccentPrimary,
    onPrimary = AccentOnAccent,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderHairline,
    error = StateError
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstaSaveTheme(content: @Composable () -> Unit) {
    // True-black identity is fixed — hardcode darkColorScheme, no light theme
    MaterialTheme(
        colorScheme = InstaSaveDarkColorScheme,
        shapes = InstaSaveShapes,
        typography = InstaSaveTypography,
        content = content
    )
}
