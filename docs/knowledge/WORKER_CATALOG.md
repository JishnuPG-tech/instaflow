# WORKER CATALOG — Background Services & Foreground Execution

## Worker / Service: `DownloadService`
- **Location**: [`app/src/main/java/com/junkfood/seal/DownloadService.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/DownloadService.kt)
- **Purpose**: Android Foreground Service ensuring process lifecycle stability.
- **Called by**: `DownloaderV2Impl`, `DownloadDialogViewModel`
- **Depends on**: `NotificationUtil`, Android OS
- **Thread**: Main / Background Task Thread
- **Decision**: KEEP
- **Reason**: Fully compatible with Android 14+ `dataSync` foreground service requirements.
- **Future modifications**: Update notification channels and action intent copy for Instagram downloads.
