package com.instasave.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.instasave.app.ui.home.HomeViewModel
import com.instasave.app.ui.navigation.MainScreen
import com.instasave.app.ui.theme.InstaSaveTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        handleIncomingIntent(intent)

        setContent {
            InstaSaveTheme {
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNull_Empty()) {
                    val extractedUrl = extractInstagramUrl(sharedText)
                    if (extractedUrl != null) {
                        homeViewModel.handleSharedUrl(extractedUrl)
                    }
                }
            }
        }
    }

    private fun String?.isNull_Empty(): Boolean = this == null || this.trim().isEmpty()

    private fun extractInstagramUrl(text: String): String? {
        val pattern = Pattern.compile("https?://(www\\.)?instagram\\.com/[^\\s]+")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }
    }
}
