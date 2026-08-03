# Reactive Data Flow Architecture

The data flow in Seal relies on Kotlin `StateFlow`, `SharedFlow`, and Room `Flow` queries to maintain unidirectional data flow (UDF) from state sources to Jetpack Compose UI components.

```mermaid
sequenceDiagram
    participant User as User / Share Sheet
    participant UI as DownloadPage / ActionSheet
    participant VM as DownloadDialogViewModel
    participant Engine as DownloaderV2 / yt-dlp
    participant Service as DownloadService
    participant DB as Room DB (VideoInfoDao)

    User->>UI: Paste URL / Share Link
    UI->>VM: Fetch Video Info (URL)
    VM->>Engine: executeMediaInfoInfoFetcherTask(url)
    Engine-->>VM: Emits TaskState (Fetching -> Success / Format List)
    VM-->>UI: Updates UI StateFlow (Show Format Selection)
    User->>UI: Select Format & Tap Download
    UI->>VM: startDownload(task)
    VM->>Service: startForegroundService()
    VM->>Engine: executeDownloadTask(task)
    Engine-->>Service: Progress Callbacks (0-100%, ETA, Speed)
    Service-->>User: Notification Progress Updates
    Engine->>DB: Insert DownloadedVideoInfo
    DB-->>UI: Observe History Flow List Update
```

---

## Unidirectional Data Flow (UDF) Patterns

1. **User Intent**: User enters URL or triggers action sheet.
2. **ViewModel State Mutation**: ViewModel processes intent, updates internal `MutableStateFlow<UiState>`, and exposes immutable `StateFlow<UiState>`.
3. **Composable Recomposition**: Compose views collect state via `collectAsStateWithLifecycle()` and re-render automatically.
4. **Database Reactivity**: Room DAO methods return `Flow<List<DownloadedVideoInfo>>`, allowing history lists to reflect new downloads instantly without polling.
