# Task Management — `DownloaderV2`, `Task`, & `TaskFactory`

## 1. Subsystem Architecture

`DownloaderV2` introduces a structured task queue system:

- **[`Task.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/download/Task.kt)**: Data class encapsulating download configuration, URL, status, progress, target file path, and format selections.
- **[`TaskFactory.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/download/TaskFactory.kt)**: Factory methods for building video, audio, playlist, and custom command tasks.
- **[`DownloaderV2.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/download/DownloaderV2.kt)**: State management interface providing `StateFlow<List<Task>>` for real-time list rendering in [`DownloadPageV2.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/downloadv2/DownloadPageV2.kt).

---

## 2. Concurrency & Queue Control

- Limits maximum active concurrent downloads via user preference (`PreferenceUtil.getMaxConcurrentDownloads()`, default: 2).
- Queues overflow tasks in `TaskState.Queued` state until active slots free up.
- Supports pausing (`cancelProcess()`), resuming, and retrying failed downloads without re-fetching media metadata.
