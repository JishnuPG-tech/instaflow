# REPOSITORY CATALOG — Data Layer

## Repository / Engine: `DownloaderV2Impl`
- **Location**: `app/src/main/java/com/junkfood/seal/download/DownloaderV2Impl.kt`
- **Purpose**: Central task execution repository and state manager.
- **Called by**: `DownloadDialogViewModel`, `QuickDownloadActivity`
- **Depends on**: `youtubedl-android`, `DownloadService`, `VideoInfoDao`
- **Thread**: `Dispatchers.IO`
- **Decision**: KEEP & EXTRACT
- **Reason**: Core download state emission engine.
- **Future modifications**: Extract interface to support `InstagramMediaExtractor`.
