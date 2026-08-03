@file:OptIn(ExperimentalMaterial3Api::class)

package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.junkfood.seal.database.InstagramMediaItem

/**
 * InstaFlow Instagram-First Media Preview & Selection Sheet
 * 
 * Replaces generic Seal format/playlist pickers for Instagram URLs.
 */
@Composable
fun InstagramMediaPreviewSheet(
    author: String,
    caption: String = "",
    thumbnailUrl: String = "",
    items: List<InstagramMediaItem> = emptyList(),
    isCarousel: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismissRequest: () -> Unit = {},
    onDownloadSingleImage: () -> Unit = {},
    onDownloadVideo: () -> Unit = {},
    onDownloadAudioOnly: () -> Unit = {},
    onDownloadSelectedItems: (List<InstagramMediaItem>) -> Unit = {},
) {
    // Selection state for carousel items
    val selectedIndices = remember(items) {
        mutableStateListOf<Int>().apply {
            // Default select all items in carousel
            indices.forEach { add(it) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header: Author Handle & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = author.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "@$author",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (isCarousel) "Carousel • ${items.size} Items" else "Instagram Media",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Type Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isCarousel) Icons.Default.PhotoLibrary else Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCarousel) "Carousel" else "Post",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            if (caption.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carousel Picker UI vs Single Media Action UI
            if (isCarousel && items.isNotEmpty()) {
                // Carousel Selection Controls (Select All / Clear)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Selected ${selectedIndices.size} of ${items.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row {
                        OutlinedButton(
                            onClick = {
                                selectedIndices.clear()
                                items.indices.forEach { selectedIndices.add(it) }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text("Select All", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { selectedIndices.clear() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Carousel Items Preview Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                ) {
                    itemsIndexed(items) { index, item ->
                        val isSelected = selectedIndices.contains(index)
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    if (isSelected) selectedIndices.remove(index)
                                    else selectedIndices.add(index)
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.thumbnailUrl.ifEmpty { item.downloadUrl })
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Item ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                // Type Tag
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = if (item.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(text = "${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Checkbox Overlay
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download Selected Action Button
                Button(
                    onClick = {
                        val selectedList = selectedIndices.map { items[it] }
                        onDownloadSelectedItems(selectedList)
                        onDismissRequest()
                    },
                    enabled = selectedIndices.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedIndices.size == items.size) "Download All (${items.size} Items)"
                        else "Download Selected (${selectedIndices.size} Items)",
                        fontWeight = FontWeight.Bold,
                    )
                }

            } else {
                // Single Item Preview Thumbnail
                if (thumbnailUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Single Media Actions (No 1080p/720p/Audio format lists)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onDownloadSingleImage()
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Download High-Res Media", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onDownloadAudioOnly()
                            onDismissRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Download Audio Only (MP3)")
                    }
                }
            }
        }
    }
}
