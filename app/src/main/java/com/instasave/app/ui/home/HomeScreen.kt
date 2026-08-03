package com.instasave.app.ui.home

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instasave.app.ui.picker.CarouselItemGrid
import com.instasave.app.ui.picker.FormatPickerBottomSheet
import com.instasave.app.ui.theme.InstagramCoral
import com.instasave.app.ui.theme.SurfaceVariantDark
import com.instasave.app.ui.theme.TextMuted
import com.instasave.app.ui.theme.TextPrimary
import com.instasave.app.ui.theme.TrueBlack

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    HomeContent(
        uiState = uiState,
        onEvent = { event ->
            if (event is HomeUiEvent.PasteFromClipboard) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clipData = clipboard?.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val pastedText = clipData.getItemAt(0).text?.toString() ?: ""
                    if (pastedText.isNotEmpty()) {
                        viewModel.onEvent(HomeUiEvent.UrlChanged(pastedText))
                    }
                }
            } else {
                viewModel.onEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // App Header Title
        Text(
            text = "InstaSave",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = (-0.5).sp
            ),
            color = InstagramCoral
        )

        Text(
            text = "Paste Instagram link to download Reel, Post or Carousel",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // URL Input Card / TextField
        OutlinedTextField(
            value = uiState.urlInput,
            onValueChange = { onEvent(HomeUiEvent.UrlChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("https://www.instagram.com/reel/...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = InstagramCoral) },
            trailingIcon = {
                if (uiState.urlInput.isNotEmpty()) {
                    IconButton(onClick = { onEvent(HomeUiEvent.ClearUrl) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear URL", tint = TextMuted)
                    }
                } else {
                    IconButton(onClick = { onEvent(HomeUiEvent.PasteFromClipboard) }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste Clipboard", tint = InstagramCoral)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceVariantDark,
                unfocusedContainerColor = SurfaceVariantDark,
                focusedBorderColor = InstagramCoral,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onEvent(HomeUiEvent.PasteFromClipboard) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceVariantDark))
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paste")
            }

            Button(
                onClick = { onEvent(HomeUiEvent.ResolveClicked) },
                enabled = !uiState.isResolving && uiState.urlInput.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InstagramCoral,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isResolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetch", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Error Banner
        uiState.error?.let { err ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onEvent(HomeUiEvent.DismissError) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Dismiss Error")
                    }
                }
            }
        }

        // Resolved Media Content Preview & Carousel Items Grid
        uiState.resolvedMedia?.let { media ->
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Resolved Media (${media.type.uppercase()})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = InstagramCoral
                    )
                    media.author?.let { author ->
                        Text(
                            text = "@$author",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    val items = media.items
                    if (!items.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Carousel Slides (${uiState.selectedCarouselIndices.size}/${items.size} Selected)",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CarouselItemGrid(
                            items = items,
                            selectedIndices = uiState.selectedCarouselIndices,
                            onToggleItem = { index -> onEvent(HomeUiEvent.ToggleCarouselItem(index)) }
                        )
                    }
                }
            }

            // Format Selection Modal Bottom Sheet
            FormatPickerBottomSheet(
                mediaInfo = media,
                onFormatSelected = { format -> onEvent(HomeUiEvent.FormatSelected(format)) },
                onDismissRequest = { onEvent(HomeUiEvent.ClearResolvedMedia) }
            )
        }
    }
}
