package com.instasave.app.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.instasave.app.core.network.generated.model.CarouselItem
import com.instasave.app.ui.theme.InstagramCoral
import com.instasave.app.ui.theme.SurfaceVariantDark
import com.instasave.app.ui.theme.TextMuted
import com.instasave.app.ui.theme.TextPrimary
import com.instasave.app.ui.theme.TrueBlack

@Composable
fun CarouselItemGrid(
    items: List<CarouselItem>,
    selectedIndices: Set<Int>,
    onToggleItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = selectedIndices.contains(index)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) InstagramCoral else SurfaceVariantDark,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onToggleItem(index) },
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (item.type == "video") Icons.Default.Videocam else Icons.Default.Image,
                            contentDescription = item.type,
                            tint = InstagramCoral,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Item ${index + 1} (${item.type.uppercase()})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = InstagramCoral,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(TrueBlack, RoundedCornerShape(10.dp))
                                    .border(1.dp, TextMuted, RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
