package com.instaflow.app.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

object ColorUtil {
    
    @Composable
    fun rememberDominantColor(thumbnailUrl: String?): Color {
        val context = LocalContext.current
        var dominantColor by remember { mutableStateOf(Color.Unspecified) }
        
        LaunchedEffect(thumbnailUrl) {
            if (thumbnailUrl.isNullOrBlank()) return@LaunchedEffect
            
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(thumbnailUrl)
                .allowHardware(false) // Required for Palette to read pixels
                .build()
                
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap
            
            if (bitmap != null) {
                Palette.from(bitmap).generate { palette ->
                    palette?.dominantSwatch?.rgb?.let {
                        dominantColor = Color(it)
                    }
                }
            }
        }
        
        return dominantColor
    }

    @Composable
    fun rememberVibrantColor(thumbnailUrl: String?, defaultColor: Color): Color {
        val context = LocalContext.current
        var vibrantColor by remember { mutableStateOf(defaultColor) }
        
        LaunchedEffect(thumbnailUrl) {
            if (thumbnailUrl.isNullOrBlank()) return@LaunchedEffect
            
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(thumbnailUrl)
                .allowHardware(false)
                .build()
                
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap
            
            if (bitmap != null) {
                Palette.from(bitmap).generate { palette ->
                    val color = palette?.vibrantSwatch?.rgb ?: palette?.mutedSwatch?.rgb
                    if (color != null) {
                        vibrantColor = Color(color)
                    }
                }
            }
        }
        
        return vibrantColor
    }
}
