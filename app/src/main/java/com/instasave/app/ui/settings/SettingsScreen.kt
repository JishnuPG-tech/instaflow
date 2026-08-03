package com.instasave.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instasave.app.ui.theme.InstagramCoral
import com.instasave.app.ui.theme.SurfaceVariantDark
import com.instasave.app.ui.theme.TextMuted
import com.instasave.app.ui.theme.TextPrimary
import com.instasave.app.ui.theme.TrueBlack

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoPasteEnabled by viewModel.autoPasteEnabled.collectAsStateWithLifecycle()
    val defaultHighQuality by viewModel.defaultHighQuality.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .padding(20.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = InstagramCoral,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // General Settings Section
        Text(
            text = "General Preferences",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        SettingSwitchTile(
            icon = Icons.Default.ContentPaste,
            title = "Auto-Paste Clipboard Link",
            subtitle = "Automatically detect Instagram link on app launch",
            checked = autoPasteEnabled,
            onCheckedChange = viewModel::toggleAutoPaste
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingSwitchTile(
            icon = Icons.Default.HighQuality,
            title = "Always Select Best Quality",
            subtitle = "Default to highest resolution format available",
            checked = defaultHighQuality,
            onCheckedChange = viewModel::toggleDefaultHighQuality
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Storage Info Section
        Text(
            text = "Storage & Downloads",
            style = MaterialTheme.typography.labelLarge,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        SettingInfoTile(
            icon = Icons.Default.Folder,
            title = "Download Location",
            subtitle = "Pictures/InstaSave & Movies/InstaSave (Scoped Storage)"
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingInfoTile(
            icon = Icons.Default.Info,
            title = "App Version",
            subtitle = "1.0.0 • Open-Source Production Grade"
        )
    }
}

@Composable
private fun SettingSwitchTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = null, tint = InstagramCoral, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = InstagramCoral,
                    checkedTrackColor = SurfaceVariantDark,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = TrueBlack
                )
            )
        }
    }
}

@Composable
private fun SettingInfoTile(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = InstagramCoral, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
    }
}
