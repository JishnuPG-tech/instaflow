package com.instaflow.app.features.instagram.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instaflow.app.features.instagram.ui.InstagramAnalyzeScreen
import com.instaflow.app.features.instagram.ui.ProgressStep

@Composable
fun InstagramNavigation(
    url: String,
    steps: List<ProgressStep> = emptyList(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    InstagramAnalyzeScreen(
        url = url,
        steps = steps,
        onCancel = onNavigateBack,
        modifier = modifier
    )
}
