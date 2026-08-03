# TECHNICAL DEBT ANALYSIS — Upstream Seal Base

## 1. Monolithic `Downloader.kt` Object

- **Problem**: [`Downloader.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/Downloader.kt) is a 600+ line Singleton `object` containing static method calls mixing CLI string construction, notification updates, progress parsing, and file handling.
- **Risk**: Hard to unit test without running real JNI `yt-dlp` commands on an Android device.
- **Remediation**: Refactor into injectable, interface-driven `DownloadTaskExecutor` classes via Koin.

## 2. Dual Downloader Abstractions (`Downloader.kt` vs `DownloaderV2.kt`)

- **Problem**: The repository contains both legacy [`Downloader.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/Downloader.kt) and partial implementation [`DownloaderV2.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/download/DownloaderV2.kt).
- **Risk**: Code duplication and inconsistent task state handling across different screens.
- **Remediation**: Consolidate 100% of download execution under `DownloaderV2`.

## 3. Direct Storage Access in ViewModels

- **Problem**: Some UI view models directly invoke `FileUtil` and `PreferenceUtil` static methods.
- **Risk**: Violates clean architecture dependency flow.
- **Remediation**: Route storage and settings requests through dedicated Repository classes (`SettingsRepository`, `MediaRepository`).
