package com.instaflow.app.features.instagram.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.instaflow.app.ui.component.CircularWavyProgressIndicator
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.blur
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class ProgressStep(
    val label: String,
    val status: StepStatus = StepStatus.PENDING
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramAnalyzeScreen(
    url: String,
    steps: List<ProgressStep>,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Note: In a real app, we would pass the preview thumbnail URL here if known.
    // For now, we focus on the Wavy indicator.
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyzing Link", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    waveCount = 10,
                    waveAmplitude = 6.dp
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Analyzing Instagram Link...",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(28.dp))
                
                Column(modifier = Modifier.fillMaxWidth(0.85f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    steps.forEach { step ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                when (step.status) {
                                    StepStatus.PENDING -> {
                                        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = MaterialTheme.colorScheme.outlineVariant) {}
                                    }
                                    StepStatus.IN_PROGRESS -> {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    StepStatus.COMPLETED -> {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                    StepStatus.FAILED -> {}
                                }
                            }
                            Spacer(modifier = Modifier.width(18.dp))
                            Text(
                                text = step.label, 
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (step.status == StepStatus.IN_PROGRESS) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (step.status == StepStatus.PENDING) 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) 
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                OutlinedButton(
                    onClick = onCancel, 
                    modifier = Modifier.fillMaxWidth(0.7f).height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { 
                    Text("Cancel", fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}
