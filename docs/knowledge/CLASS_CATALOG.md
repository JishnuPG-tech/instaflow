# CLASS CATALOG — Living Engineering Knowledge Base

## Class: `DownloaderV2`
- **Location**: [`app/src/main/java/com/junkfood/seal/download/DownloaderV2.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/download/DownloaderV2.kt)
- **Purpose**: Main download orchestration interface and state flow queue manager.
- **Called by**: `DownloadDialogViewModel`, `DownloadPageV2`
- **Depends on**: `DownloadService`, `TaskFactory`, `VideoInfoDao`
- **Thread**: Coroutine IO / Main
- **Decision**: KEEP
- **Reason**: Stable upstream task queue and state emission architecture.
- **Future modifications**: Integrate pluggable `InstagramMediaExtractor` pipeline.

---

## Class: `App`
- **Location**: [`app/src/main/java/com/junkfood/seal/App.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/App.kt)
- **Purpose**: Application lifecycle initialization (MMKV, Koin, `youtubedl-android`, `Aria2c`).
- **Called by**: Android OS
- **Depends on**: Koin, MMKV, `YoutubeDL`, `FFmpeg`, `Aria2c`
- **Thread**: Main / IO Scope
- **Decision**: REUSE WITH MODIFICATION
- **Reason**: Essential initialization logic.
- **Future modifications**: Update namespace during Phase 5 rebranding.

---

## Class: `MainActivity`
- **Location**: [`app/src/main/java/com/junkfood/seal/MainActivity.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/MainActivity.kt)
- **Purpose**: Single-activity host for Navigation Compose UI.
- **Called by**: Launcher Intent
- **Depends on**: `AppEntry`, Navigation Host
- **Thread**: Main Looper
- **Decision**: KEEP
- **Reason**: Core Android single-activity architecture.
- **Future modifications**: None required until Phase 5.
