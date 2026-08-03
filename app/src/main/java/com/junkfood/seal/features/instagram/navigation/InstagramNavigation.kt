package com.junkfood.seal.features.instagram.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.junkfood.seal.features.instagram.ui.InstagramAnalyzeScreen

@Composable
fun InstagramNavigation(
    url: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // WP 1.1 Routes directly to InstagramAnalyzeScreen
    InstagramAnalyzeScreen(
        url = url,
        onCancel = onNavigateBack,
        modifier = modifier
    )
}
