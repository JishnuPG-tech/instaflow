package com.instasave.app.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.instasave.app.ui.theme.TrueBlack

@Composable
fun DownloadsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Active Downloads Queue",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HistoryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Download History",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Application Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
