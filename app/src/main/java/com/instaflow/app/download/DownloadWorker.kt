package com.instaflow.app.download

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.instaflow.app.util.DownloadUtil
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val taskJson = inputData.getString(KEY_TASK) ?: return Result.failure()
        val task = try {
            json.decodeFromString<Task>(taskJson)
        } catch (e: Exception) {
            return Result.failure()
        }

        Log.i(TAG, "[Worker] Starting background download for task: ${task.id}")
        
        val downloadResult = try {
            // First fetch info if not present
            val infoRes = DownloadUtil.fetchVideoInfoFromUrl(
                url = task.url,
                playlistIndex = if (task.type is Task.TypeInfo.Playlist) task.type.index else null,
                preferences = task.preferences,
                taskKey = task.id
            )
            
            if (infoRes.isFailure) {
                val error = infoRes.exceptionOrNull()
                Log.e(TAG, "[Worker] Fetch info failed: ${error?.message}")
                return Result.retry()
            }
            
            val info = infoRes.getOrThrow()
            
            DownloadUtil.downloadVideo(
                videoInfo = info,
                playlistUrl = task.url,
                playlistItem = if (task.type is Task.TypeInfo.Playlist) task.type.index else 0,
                taskId = task.id,
                downloadPreferences = task.preferences,
                progressCallback = { progress, _, text ->
                    val data = workDataOf(PROGRESS to progress, TEXT to text)
                    // Note: setProgress is a suspend function. 
                    // We skip granular progress for now to ensure worker stability.
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "[Worker] Download exception: ${e.message}")
            return Result.retry()
        }

        return if (downloadResult.isSuccess) {
            Log.i(TAG, "[Worker] Download completed successfully: ${task.id}")
            val files = downloadResult.getOrNull()
            Result.success(workDataOf(OUTPUT_PATH to (files?.firstOrNull() ?: "")))
        } else {
            Log.e(TAG, "[Worker] Download failed: ${task.id}")
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "DownloadWorker"
        const val KEY_TASK = "key_task"
        const val PROGRESS = "progress"
        const val TEXT = "text"
        const val OUTPUT_PATH = "output_path"
    }
}
