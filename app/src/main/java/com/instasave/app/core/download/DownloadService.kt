package com.instasave.app.core.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.instasave.app.core.download.model.DownloadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject
    lateinit var queueManager: DownloadQueueManager

    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "instasave_download_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Downloading media...", 0, 0))
        observeQueue()
    }

    private fun observeQueue() {
        scope.launch {
            queueManager.tasks.collect { tasks ->
                val active = tasks.firstOrNull { it.state == DownloadState.DOWNLOADING }
                if (active != null) {
                    val speedMb = active.speedBytesPerSec / (1024 * 1024f)
                    val speedText = String.format("%.1f MB/s", speedMb)
                    val notification = buildNotification(
                        "Downloading ${active.fileName} ($speedText)",
                        active.progressPercent,
                        100
                    )
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                } else if (tasks.none { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED }) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress for ongoing Instagram downloads"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, progress: Int, max: Int) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("InstaSave")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(max, progress, false)
            .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
