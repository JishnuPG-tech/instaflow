@file:OptIn(ExperimentalMaterial3Api::class)

package com.instaflow.app.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.instaflow.app.database.InstagramMediaItem
import com.instaflow.app.features.instagram.models.InstagramFormat
import com.instaflow.app.util.InstagramUrlType
import com.instaflow.app.util.toFileSizeText

@Composable
fun InstagramMediaPreviewSheet(
    author: String,
    caption: String = "",
    thumbnailUrl: String = "",
    mediaTypeLabel: String = "Post",
    duration: String = "",
    urlType: InstagramUrlType = InstagramUrlType.UNKNOWN,
    items: List<InstagramMediaItem> = emptyList(),
    isCarousel: Boolean = false,
    videoQualityOptions: List<InstagramFormat> = emptyList(),
    audioQualityOptions: List<InstagramFormat> = emptyList(),
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissRequest: () -> Unit = {},
    onDownloadWithFormat: (InstagramFormat) -> Unit = {},
    onDownloadSelectedItems: (List<InstagramMediaItem>) -> Unit = {},
) {
    val selectedIndices = remember(items) {
        mutableStateListOf<Int>().apply {
            indices.forEach { add(it) }
        }
    }

    var selectedFormat by remember(videoQualityOptions, audioQualityOptions) {
        mutableStateOf(videoQualityOptions.firstOrNull() ?: audioQualityOptions.firstOrNull())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 12.dp)
                .size(width = 32.dp, height = 4.dp)
                .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), shape = CircleShape)
        )

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = author.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = if (author.startsWith("@")) author else "@$author",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Instagram Media",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = when {
                            isCarousel -> Icons.Default.PhotoLibrary
                            mediaTypeLabel.contains("Story") -> Icons.Default.History
                            mediaTypeLabel.contains("Profile") -> Icons.Default.Person
                            mediaTypeLabel.contains("Video") || mediaTypeLabel.contains("Reel") -> Icons.Default.Movie
                            else -> Icons.Default.Image
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (duration.isNotEmpty() && duration != "Photo") "$mediaTypeLabel • $duration" else mediaTypeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (caption.isNotEmpty()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isCarousel && items.isNotEmpty()) {
            // Carousel Grid UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Selected ${selectedIndices.size} of ${items.size}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { selectedIndices.clear(); items.indices.forEach { selectedIndices.add(it) } },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Select All", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { selectedIndices.clear() },
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Text("Clear", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    Card(
                        modifier = Modifier.aspectRatio(1f).clickable {
                            if (isSelected) selectedIndices.remove(index) else selectedIndices.add(index)
                        }.border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = item.thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            if (item.isVideo) {
                                Icon(Icons.Default.Videocam, null, modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).size(16.dp), tint = Color.White)
                            }
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onDownloadSelectedItems(selectedIndices.map { items[it] }); onDismissRequest() },
                enabled = selectedIndices.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(Modifier.width(8.dp))
                Text("Download Selected Items", fontWeight = FontWeight.Black)
            }

        } else {
            // Single Media UI
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (thumbnailUrl.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            AsyncImage(
                                model = thumbnailUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
                
                val allFormats = videoQualityOptions + audioQualityOptions
                if (allFormats.isNotEmpty()) {
                    item {
                        Text(
                            text = "Select Quality",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(allFormats) { format ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedFormat = format },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFormat == format) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = format.resolutionLabel,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedFormat == format) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    val sizeText = format.fileSizeApprox.toFileSizeText()
                                    if (sizeText.isNotEmpty()) {
                                        Text(
                                            text = sizeText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selectedFormat == format) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                RadioButton(
                                    selected = selectedFormat == format,
                                    onClick = { selectedFormat = format }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            selectedFormat?.let { onDownloadWithFormat(it) } ?: run {
                                onDownloadWithFormat(InstagramFormat("best", "Original Quality", 0, 0, "jpg", 0, false))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Download Media", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
