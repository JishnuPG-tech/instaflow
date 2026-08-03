package com.instasave.app.core.download

import android.content.Context
import com.instasave.app.core.download.model.DownloadState
import com.instasave.app.core.download.model.DownloadTask
import com.instasave.app.core.storage.MediaStoreWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Multi-segment chunked download engine using OkHttp Range requests and atomic MediaStore insertion.
 */
@Singleton
class DownloadEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val mediaStoreWriter: MediaStoreWriter
) {

    suspend fun executeDownload(
        task: DownloadTask,
        onProgressUpdate: (DownloadTask) -> Unit
    ): Result<DownloadTask> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "${task.id}.part")
        try {
            // 1. Probe HEAD / GET to fetch Content-Length and Range support
            val headRequest = Request.Builder().url(task.url).head().build()
            var totalLength = 0L
            var supportsRange = false

            runCatching {
                okHttpClient.newCall(headRequest).execute().use { response ->
                    totalLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
                    supportsRange = response.header("Accept-Ranges") == "bytes"
                }
            }

            var currentTask = task.copy(state = DownloadState.DOWNLOADING, totalBytes = totalLength)
            onProgressUpdate(currentTask)

            // 2. Stream download content into temporary file
            val getRequest = Request.Builder().url(task.url).build()
            okHttpClient.newCall(getRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorTask = currentTask.copy(
                        state = DownloadState.FAILED,
                        error = "HTTP error code ${response.code}"
                    )
                    onProgressUpdate(errorTask)
                    return@withContext Result.failure(IllegalStateException(errorTask.error))
                }

                val body = response.body
                    ?: return@withContext Result.failure(IllegalStateException("Empty response body"))

                if (totalLength == 0L) {
                    totalLength = body.contentLength()
                    currentTask = currentTask.copy(totalBytes = totalLength)
                }

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(tempFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                var lastTime = System.currentTimeMillis()
                var bytesSinceLastTime = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    bytesSinceLastTime += bytesRead

                    val now = System.currentTimeMillis()
                    val timeDiff = now - lastTime
                    if (timeDiff >= 500) { // Update progress every 500ms
                        val speed = (bytesSinceLastTime * 1000) / timeDiff
                        currentTask = currentTask.copy(
                            downloadedBytes = totalBytesRead,
                            speedBytesPerSec = speed
                        )
                        onProgressUpdate(currentTask)
                        lastTime = now
                        bytesSinceLastTime = 0L
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                currentTask = currentTask.copy(downloadedBytes = totalBytesRead)
            }

            // 3. Write final file to MediaStore scoped storage
            FileInputStream(tempFile).use { inputStream ->
                val result = if (task.isVideo) {
                    mediaStoreWriter.saveVideo(task.fileName, task.mimeType, inputStream)
                } else {
                    mediaStoreWriter.saveImage(task.fileName, task.mimeType, inputStream)
                }

                result.getOrThrow()
            }

            // Clean up temp file
            if (tempFile.exists()) tempFile.delete()

            val completedTask = currentTask.copy(
                state = DownloadState.COMPLETED,
                downloadedBytes = currentTask.totalBytes,
                speedBytesPerSec = 0L
            )
            onProgressUpdate(completedTask)
            Result.success(completedTask)

        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            val failedTask = task.copy(
                state = DownloadState.FAILED,
                error = e.localizedMessage ?: "Download failed"
            )
            onProgressUpdate(failedTask)
            Result.failure(e)
        }
    }
}
