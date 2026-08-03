package com.instasave.app.core.download

import com.instasave.app.core.download.model.DownloadState
import com.instasave.app.core.download.model.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadQueueManager @Inject constructor(
    private val downloadEngine: DownloadEngine
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks: StateFlow<List<DownloadTask>> = _tasks.asStateFlow()

    fun enqueueDownload(
        url: String,
        fileName: String,
        mimeType: String = "video/mp4",
        isVideo: Boolean = true
    ): DownloadTask {
        val task = DownloadTask(
            url = url,
            fileName = fileName,
            mimeType = mimeType,
            isVideo = isVideo
        )
        _tasks.update { it + task }
        startDownloadTask(task)
        return task
    }

    private fun startDownloadTask(task: DownloadTask) {
        scope.launch {
            downloadEngine.executeDownload(task) { updatedTask ->
                updateTaskState(updatedTask)
            }
        }
    }

    private fun updateTaskState(updatedTask: DownloadTask) {
        _tasks.update { list ->
            list.map { item ->
                if (item.id == updatedTask.id) updatedTask else item
            }
        }
    }

    fun cancelTask(taskId: String) {
        _tasks.update { list ->
            list.map { item ->
                if (item.id == taskId) item.copy(state = DownloadState.CANCELLED) else item
            }
        }
    }
}
