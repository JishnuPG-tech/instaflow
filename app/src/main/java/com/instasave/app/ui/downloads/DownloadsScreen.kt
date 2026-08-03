package com.instasave.app.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instasave.app.core.download.model.DownloadState
import com.instasave.app.core.download.model.DownloadTask
import com.instasave.app.ui.theme.InstagramCoral
import com.instasave.app.ui.theme.SurfaceVariantDark
import com.instasave.app.ui.theme.TextMuted
import com.instasave.app.ui.theme.TextPrimary
import com.instasave.app.ui.theme.TrueBlack

@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .padding(20.dp)
    ) {
        Text(
            text = "Active Downloads",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = InstagramCoral,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active or recent downloads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tasks, key = { it.id }) { task ->
                    DownloadTaskCard(
                        task = task,
                        onCancel = { viewModel.cancelTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadTaskCard(
    task: DownloadTask,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.fileName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (task.state == DownloadState.DOWNLOADING || task.state == DownloadState.QUEUED) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel Download", tint = TextMuted)
                    }
                } else if (task.state == DownloadState.COMPLETED) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = InstagramCoral)
                } else if (task.state == DownloadState.FAILED) {
                    Icon(Icons.Default.Error, contentDescription = "Failed", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = InstagramCoral,
                trackColor = TrueBlack
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val speedMb = task.speedBytesPerSec / (1024 * 1024f)
                val speedText = if (task.state == DownloadState.DOWNLOADING) String.format("%.1f MB/s", speedMb) else task.state.name
                Text(
                    text = speedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = "${task.progressPercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = InstagramCoral,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
