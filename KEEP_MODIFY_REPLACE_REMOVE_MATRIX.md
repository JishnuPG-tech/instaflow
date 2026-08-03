# KEEP / MODIFY / REPLACE / REMOVE / EXTRACT / GENERALIZE MATRIX

| Subsystem / File | Action | Justification based on Existing Implementation |
| :--- | :--- | :--- |
| **`youtubedl-android` Engine** | **KEEP** | [`App.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/App.kt) initializes native `yt-dlp`, `ffmpeg`, and `aria2c` binaries. Provides mature JNI process execution without cloud server requirements. |
| **`DownloadService.kt`** | **KEEP** | Android 14+ `dataSync` foreground service handling lifecycle stability when app is backgrounded. |
| **MMKV Storage (`PreferenceUtil.kt`)** | **KEEP** | Native mmap key-value storage delivers near-instant read/writes without main-thread blocking. |
| **Room Database (`AppDatabase.kt`)** | **KEEP** & **MODIFY** | Room infrastructure is mature. Modify `DownloadedVideoInfo` schema to add Instagram-specific columns (`mediaType`, `caption`, `username`). |
| **`Downloader.kt` Monolith** | **GENERALIZE** & **EXTRACT** | Abstract hardcoded `yt-dlp` CLI calls behind a `MediaExtractor` interface allowing fallback to native HTTP API extraction when `yt-dlp` breaks on Instagram. |
| **Format Selection Dialog (`DownloadSettingsDialog.kt`)** | **REPLACE** | Replace generic video resolution/codec selectors (1080p, MP4, WEBM) with Instagram Media Resolution & Quality Picker. |
| **Playlist Index Picker (`PlaylistSelectionDialog.kt`)** | **REPLACE** | Replace YouTube playlist selector with an Instagram Carousel Multi-Media Picker. |
| **SponsorBlock Integration** | **REMOVE** | Found in `PreferenceUtil.kt` & settings pages. SponsorBlock is YouTube-specific and useless for Instagram content. |
| **Subtitle Extraction & Embedding** | **REMOVE** | Found in `Downloader.kt` (`--write-sub`). Instagram does not use embedded VTT/SRT subtitles in public posts. |
| **Netscape Cookie System** | **KEEP** & **MODIFY** | Keep Netscape export engine in `FileUtil.kt`. Add an integrated In-App WebView Cookie Sync helper to capture `sessionid`. |
| **`:color` Library Module** | **KEEP** | Dynamic Material 3 HSL color extraction works perfectly for InstaFlow branding and dynamic dark themes. |
