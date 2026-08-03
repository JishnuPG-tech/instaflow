package com.instasave.app.ui.downloads

import androidx.lifecycle.ViewModel
import com.instasave.app.core.download.DownloadQueueManager
import com.instasave.app.core.download.model.DownloadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val queueManager: DownloadQueueManager
) : ViewModel() {

    val tasks: StateFlow<List<DownloadTask>> = queueManager.tasks

    fun cancelTask(taskId: String) {
        queueManager.cancelTask(taskId)
    }
}
