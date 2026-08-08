package com.instaflow.app.download

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.asFlow
import com.instaflow.app.App
import com.instaflow.app.R
import com.instaflow.app.download.Task.DownloadState
import com.instaflow.app.download.Task.DownloadState.Canceled
import com.instaflow.app.download.Task.DownloadState.Completed
import com.instaflow.app.download.Task.DownloadState.Error
import com.instaflow.app.download.Task.DownloadState.FetchingInfo
import com.instaflow.app.download.Task.DownloadState.Idle
import com.instaflow.app.download.Task.DownloadState.ReadyWithInfo
import com.instaflow.app.download.Task.DownloadState.Running
import com.instaflow.app.download.Task.RestartableAction.Download
import com.instaflow.app.download.Task.RestartableAction.FetchInfo
import com.instaflow.app.download.Task.TypeInfo
import com.instaflow.app.util.DownloadUtil
import com.instaflow.app.util.FileUtil
import com.instaflow.app.util.NotificationUtil
import com.instaflow.app.util.PreferenceUtil
import com.instaflow.app.util.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

private const val TAG = "DownloaderV2"

private const val MAX_CONCURRENCY = 3

interface DownloaderV2 {
    fun getTaskStateMap(): SnapshotStateMap<Task, Task.State>

    fun cancel(task: Task): Boolean

    fun cancel(taskId: String): Boolean {
        return getTaskStateMap().keys.find { it.id == taskId }?.let { cancel(it) } ?: false
    }

    fun restart(task: Task)

    /** Enqueue a [Task] with an empty [Task.State] */
    fun enqueue(task: Task)

    fun enqueue(task: Task, state: Task.State)

    fun enqueue(taskWithState: TaskFactory.TaskWithState) {
        val (task, state) = taskWithState
        enqueue(task, state)
    }

    fun remove(task: Task): Boolean
}

internal object FakeDownloaderV2 : DownloaderV2 {
    override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
        return mutableStateMapOf()
    }

    override fun cancel(task: Task): Boolean {
        return false
    }

    override fun restart(task: Task) {}

    override fun enqueue(task: Task) {}

    override fun enqueue(task: Task, state: Task.State) {}

    override fun remove(task: Task): Boolean {
        return true
    }
}

/**
 * TODO:
 *     - Notification
 *     - Custom commands
 *     - States for ViewModels
 */
@OptIn(FlowPreview::class)
class DownloaderV2Impl(private val appContext: Context) : DownloaderV2, KoinComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val taskStateMap = mutableStateMapOf<Task, Task.State>()
    private val snapshotFlow = snapshotFlow { taskStateMap.toMap() }
    private val workManager = WorkManager.getInstance(appContext)
    private val json = Json { ignoreUnknownKeys = true }

    init {
        scope.launch(Dispatchers.Default) {
            snapshotFlow
                .onEach { doYourWork() }
                .map { it.countRunning() }
                .distinctUntilChanged()
                .collect { if (it > 0) App.startService() else App.stopService() }
        }

        // Observe WorkManager for carousel items
        scope.launch(Dispatchers.Main) {
            workManager.getWorkInfosByTagLiveData("download_task").asFlow().collect { workInfos ->
                workInfos.forEach { info ->
                    val taskId = info.tags.find { it != "download_task" && it != "com.instaflow.app.download.DownloadWorker" }
                    if (taskId != null) {
                        val task = taskStateMap.keys.find { it.id == taskId }
                        if (task != null) {
                            when (info.state) {
                                androidx.work.WorkInfo.State.SUCCEEDED -> {
                                    val path = info.outputData.getString(DownloadWorker.OUTPUT_PATH)
                                    task.downloadState = Completed(path)
                                }
                                androidx.work.WorkInfo.State.FAILED -> {
                                    task.downloadState = Error(action = Download)
                                }
                                androidx.work.WorkInfo.State.RUNNING -> {
                                    val progress = info.progress.getFloat(DownloadWorker.PROGRESS, 0f)
                                    val text = info.progress.getString(DownloadWorker.TEXT) ?: ""
                                    task.downloadState = Running(taskId = taskId, progress = progress / 100f, progressText = text)
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        scope.launch(Dispatchers.IO) {
            // don't write before we read
            enqueueFromBackup()

            snapshotFlow
                .map { it.filter { it.value.downloadState !is Completed } }
                .distinctUntilChanged()
                .collect {
                    it.forEach { Log.d(TAG, it.value.viewState.title) }
                    PreferenceUtil.encodeTaskListBackup(it)
                }
        }
    }

    private fun enqueueFromBackup() {
        val taskList =
            PreferenceUtil.decodeTaskListBackup()
                .filter { it.value.downloadState !is Completed && (it.key.url.startsWith("http") || it.key.url.startsWith("https")) }
                .mapValues { (_, state) ->
                    val preState = state.downloadState
                    val downloadState =
                        when (preState) {
                            is FetchingInfo,
                            Idle -> {
                                Canceled(action = FetchInfo)
                            }
                            is Running -> {
                                Canceled(action = Download, progress = preState.progress)
                            }

                            ReadyWithInfo -> {
                                Canceled(action = Download, progress = null)
                            }
                            else -> {
                                preState
                            }
                        }
                    state.copy(downloadState = downloadState)
                }
        taskList.forEach(::enqueue)
    }

    private fun Map<Task, Task.State>.countRunning(): Int = count { (_, state) ->
        state.downloadState is Running || state.downloadState is FetchingInfo
    }

    override fun getTaskStateMap(): SnapshotStateMap<Task, Task.State> {
        return taskStateMap
    }

    override fun enqueue(task: Task) {
        taskStateMap +=
            task to Task.State(Idle, null, Task.ViewState(url = task.url, title = task.url))
    }

    override fun enqueue(task: Task, state: Task.State) {
        taskStateMap += task to state
    }

    /**
     * Noted the caller is responsible for stopping the [task] before removing it
     *
     * @return true if the task was removed
     */
    override fun remove(task: Task): Boolean {
        if (taskStateMap.contains(task)) {
            taskStateMap.remove(task)
            return true
        }
        return false
    }

    override fun cancel(task: Task): Boolean = task.cancelImpl()

    override fun restart(task: Task) {
        task.restartImpl()
    }

    private var Task.state: Task.State
        get() = taskStateMap[this]!!
        set(value) {
            taskStateMap[this] = value
        }

    private var Task.downloadState: DownloadState
        get() = state.downloadState
        set(value) {
            val prevState = state
            taskStateMap[this] = prevState.copy(downloadState = value)
        }

    private var Task.info: VideoInfo?
        get() = state.videoInfo
        set(value) {
            val prevState = state
            taskStateMap[this] = prevState.copy(videoInfo = value)
        }

    private var Task.viewState: Task.ViewState
        get() = state.viewState
        set(value) {
            val prevState = state
            taskStateMap[this] = prevState.copy(viewState = value)
        }

    private val notificationIdMap = java.util.concurrent.ConcurrentHashMap<String, Int>()
    private val notificationIdCounter = java.util.concurrent.atomic.AtomicInteger(1000)

    private val Task.notificationId: Int
        get() = notificationIdMap.getOrPut(id) { notificationIdCounter.incrementAndGet() }

    /** Processes pending tasks, prioritizing downloads. */
    private fun doYourWork() {
        val runningCount = taskStateMap.countRunning()
        if (runningCount >= MAX_CONCURRENCY) {
            Log.d(TAG, "[Pipeline] Max concurrency reached ($runningCount/$MAX_CONCURRENCY). Waiting.")
            return
        }

        taskStateMap.entries
            .sortedBy { (_, state) -> state.downloadState }
            .firstOrNull { (_, state) ->
                state.downloadState == ReadyWithInfo || state.downloadState == Idle
            }
            ?.let { (task, state) ->
                Log.i(TAG, "[Pipeline] doYourWork: picked task ${task.id} in state ${state.downloadState}")
                when (state.downloadState) {
                    Idle -> task.prepare()
                    ReadyWithInfo -> task.download()
                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
    }

    private fun Task.prepare() {
        check(downloadState == Idle)
        if (type is TypeInfo.CustomCommand) {
            execute()
        } else {
            fetchInfo()
        }
    }

    private fun Task.fetchInfo() {
        check(downloadState == Idle)
        val task = this
        val taskInfo = task.type
        val playlistIndex = if (taskInfo is TypeInfo.Playlist) taskInfo.index else null
        Log.i(TAG, "[Pipeline] DownloaderV2 fetchInfo start — taskId=$id, url=$url")
        val startTime = System.currentTimeMillis()
        scope
            .launch(Dispatchers.Default) {
                DownloadUtil.fetchVideoInfoFromUrl(
                        url = url,
                        playlistIndex = playlistIndex,
                        preferences = preferences,
                        taskKey = id,
                    )
                    .onSuccess {
                        val elapsed = System.currentTimeMillis() - startTime
                        Log.i(TAG, "[Pipeline] DownloaderV2 fetchInfo success (${elapsed}ms) — taskId=$id, title=${it.title}, extractor=${it.extractorKey}")
                        info = it
                        downloadState = ReadyWithInfo
                        viewState = Task.ViewState.fromVideoInfo(it)
                    }
                    .onFailure { throwable ->
                        val elapsed = System.currentTimeMillis() - startTime
                        if (throwable is YoutubeDL.CanceledException) {
                            Log.i(TAG, "[Pipeline] DownloaderV2 fetchInfo canceled (${elapsed}ms) — taskId=$id")
                            return@onFailure
                        }
                        Log.e(TAG, "[Pipeline] DownloaderV2 fetchInfo failed (${elapsed}ms) — taskId=$id, url=$url: ${throwable.message}")
                        throwable.printStackTrace()
                        task.downloadState = Error(throwable = throwable, action = FetchInfo)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.download_error_msg,
                            notificationId = notificationId,
                            report = "URL: $url\n\n${throwable.stackTraceToString()}",
                        )
                    }
            }
            .also { job -> downloadState = FetchingInfo(job = job, taskId = id) }
    }

    private fun Task.download() {
        check(downloadState == ReadyWithInfo && info != null)
        
        // If it's a carousel item or we want extra reliability, use WorkManager
        if (type is TypeInfo.Playlist) {
            Log.i(TAG, "[Pipeline] Enqueuing carousel item to WorkManager: $id")
            val taskJson = json.encodeToString(this)
            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf(DownloadWorker.KEY_TASK to taskJson))
                .addTag("download_task")
                .addTag(id)
                .build()
            
            workManager.enqueue(workRequest)
            
            // Still track it in memory for UI, but mark as running
            downloadState = Running(job = Job(), taskId = id) // Placeholder job
            return
        }

        if (type is TypeInfo.CustomCommand) {
            execute()
            return
        }
        val taskInfo = type
        val playlistIndex = if (taskInfo is TypeInfo.Playlist) taskInfo.index else 0
        Log.i(TAG, "[Pipeline] DownloaderV2 download start — taskId=$id, url=$url, index=$playlistIndex, formatId='${preferences.formatIdString}'")
        val startTime = System.currentTimeMillis()
        scope
            .launch(Dispatchers.Default) {
                DownloadUtil.downloadVideo(
                        videoInfo = info,
                        playlistUrl = url,
                        playlistItem = playlistIndex,
                        taskId = id,
                        downloadPreferences = preferences,
                        progressCallback = { progressPercentage, _, text ->
                            val progress = progressPercentage / 100f
                            when (val preState = downloadState) {
                                is Running -> {
                                    downloadState =
                                        preState.copy(progress = progress, progressText = text)
                                    NotificationUtil.notifyProgress(
                                        notificationId = notificationId,
                                        progress = progressPercentage.toInt(),
                                        text = text,
                                        title = viewState.title,
                                        taskId = id,
                                    )
                                }
                                else -> {}
                            }
                        },
                    )
                    .onSuccess { pathList ->
                        val elapsed = System.currentTimeMillis() - startTime
                        val outputPath = pathList.firstOrNull() ?: "unknown"
                        Log.i(TAG, "[Pipeline] DownloaderV2 download success (${elapsed}ms) — taskId=$id, output=$outputPath")
                        downloadState = Completed(pathList.firstOrNull())

                        val text =
                            appContext.getString(
                                if (pathList.isEmpty()) R.string.status_completed
                                else R.string.download_finish_notification
                            )
                        FileUtil.createIntentForOpeningFile(pathList.firstOrNull()).run {
                            NotificationUtil.finishNotification(
                                notificationId,
                                title = viewState.title,
                                text = text,
                                intent =
                                    if (this != null)
                                        PendingIntent.getActivity(
                                            appContext,
                                            0,
                                            this,
                                            PendingIntent.FLAG_IMMUTABLE,
                                        )
                                    else null,
                            )
                        }
                    }
                    .onFailure { throwable ->
                        val elapsed = System.currentTimeMillis() - startTime
                        if (throwable is YoutubeDL.CanceledException) {
                            Log.i(TAG, "[Pipeline] DownloaderV2 download canceled (${elapsed}ms) — taskId=$id")
                            return@onFailure
                        }
                        Log.e(TAG, "[Pipeline] DownloaderV2 download failed (${elapsed}ms) — taskId=$id, url=$url: ${throwable.message}")
                        throwable.printStackTrace()
                        downloadState = Error(throwable = throwable, action = Download)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.fetch_info_error_msg,
                            notificationId = notificationId,
                            report = "URL: $url\nFormat: ${preferences.formatIdString}\n\n${throwable.stackTraceToString()}",
                        )
                    }
            }
            .also { job -> downloadState = Running(job = job, taskId = id) }
    }

    private fun Task.cancelImpl(): Boolean {
        when (val preState = downloadState) {
            is DownloadState.Cancelable -> {
                val res = YoutubeDL.destroyProcessById(preState.taskId)
                if (res) {
                    preState.job.cancel()
                    val progress = if (preState is Running) preState.progress else null
                    NotificationUtil.cancelNotification(notificationId)
                    downloadState =
                        DownloadState.Canceled(action = preState.action, progress = progress)
                }
                return res
            }
            Idle -> {
                downloadState = DownloadState.Canceled(action = FetchInfo)
            }
            ReadyWithInfo -> {
                downloadState = DownloadState.Canceled(action = Download)
            }

            else -> {
                return false
            }
        }
        return true
    }

    private fun Task.restartImpl() {
        when (val preState = downloadState) {
            is DownloadState.Restartable -> {
                downloadState =
                    when (preState.action) {
                        Download -> ReadyWithInfo
                        FetchInfo -> Idle
                    }
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    /**
     * Execute a custom command task
     *
     * @see Task.TypeInfo.CustomCommand
     */
    private fun Task.execute() {
        check(downloadState == Idle)
        check(type is TypeInfo.CustomCommand)
        val template = type.template
        scope
            .launch {
                DownloadUtil.executeCustomCommandTask(url, id, template, preferences) {
                        progressPercentage,
                        _,
                        text ->
                        val progress = progressPercentage / 100f
                        when (val preState = downloadState) {
                            is Running -> {
                                downloadState =
                                    preState.copy(progress = progress, progressText = text)
                                NotificationUtil.makeNotificationForCustomCommand(
                                    notificationId = notificationId,
                                    taskId = id,
                                    progress = progressPercentage.toInt(),
                                    templateName = template.name,
                                    taskUrl = url,
                                    text = text,
                                )
                            }
                            else -> {}
                        }
                    }
                    .onFailure { throwable ->
                        if (throwable is YoutubeDL.CanceledException) {
                            return@onFailure
                        }
                        downloadState = Error(throwable = throwable, action = Download)
                        NotificationUtil.notifyError(
                            title = viewState.title,
                            textId = R.string.fetch_info_error_msg,
                            notificationId = notificationId,
                            report = throwable.stackTraceToString(),
                        )
                    }
                    .onSuccess {
                        downloadState = Completed(null)

                        val text = appContext.getString(R.string.status_completed)

                        NotificationUtil.finishNotification(
                            notificationId = notificationId,
                            title = viewState.title,
                            text = text,
                            intent = null,
                        )
                    }
            }
            .also { downloadState = Running(job = it, taskId = id) }
    }
}
