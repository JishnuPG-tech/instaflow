# WP 1.3 RUNTIME VERIFICATION & FUNCTIONAL MAPPING REPORT

## 1. Executive Summary

- **Work Package**: `WP 1.3: Runtime Verification & Functional Mapping`
- **Target Application**: Upstream [JunkFood02/Seal](https://github.com/JunkFood02/Seal) (`1.12.0`) vs. [InstaFlow](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow) Target
- **Environment**: Cloud Codespace (`Linux x86_64 Ubuntu 22.04 LTS`, OpenJDK 21) & Local Runtime Analysis
- **Status**: 🟢 PASS (Functional Baseline Mapped & Certified)

---

## 2. Functional Mapping Matrix (Upstream Seal → InstaFlow Target)

| Subsystem / Feature | Current Upstream Seal Behavior | InstaFlow Adaptation Category | Target Work Package |
| :--- | :--- | :--- | :--- |
| **1. App Startup** | Initialises MMKV, Koin DI, `youtubedl-android`, `ffmpeg`, `aria2c` in [`App.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/App.kt). Applies Material 3 dynamic colors. | **KEEP** | `WP 6.1` (Rebranding only in Phase 6) |
| **2. Home Screen** | Text URL input field with automatic clipboard URL paste button in [`DownloadPage.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/DownloadPage.kt). | **MODIFY** | `WP 2.1` (Instagram URL validator) & `WP 4.1` (Home UX) |
| **3. Download Flow** | Triggers `yt-dlp -j` metadata fetch, shows codec & resolution picker dialog ([`DownloadSettingsDialog.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/DownloadSettingsDialog.kt)). | **REPLACE** | `WP 4.2` & `WP 4.3` (Instagram Action Sheet & Quality Picker) |
| **4. Settings Hub** | Multi-page preferences ([`SettingsPage.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/settings/SettingsPage.kt)) including SponsorBlock, Subtitles, Directory, Network. | **MODIFY** & **REMOVE** | `WP 4.6` (Cookie Sync) & `WP 5.1`-`5.4` (Strip SponsorBlock/Subtitles) |
| **5. Download History** | Room DB query displaying list of downloaded items ([`VideoListPage.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/videolist/VideoListPage.kt)) with thumbnail, open file, and delete actions. | **MODIFY** | `WP 3.6` (Room `MIGRATION_1_2`) & `WP 4.5` (Media badges) |
| **6. Task Queue** | Interactive task action sheet ([`ActionSheet.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/downloadv2/ActionSheet.kt)) and task card ([`VideoCardV2.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/downloadv2/VideoCardV2.kt)). | **KEEP** & **MODIFY** | `WP 3.9` & `WP 3.10` (Carousel item progress & retry) |
| **7. Notifications** | Low-importance notification channel with progress bar and cancel/pause actions ([`NotificationUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/NotificationUtil.kt)). | **KEEP** | `WP 4.4` (Queue & Notification copy) |
| **8. Cookie Import** | Manual file picker for Netscape `cookies.txt` ([`CookiesViewModel.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/settings/network/CookiesViewModel.kt)). | **KEEP** & **NEW** | `WP 4.6` (In-App WebView Cookie Sync) |
| **9. Share Sheet Intent** | Intercepts `android.intent.action.SEND` in translucent [`QuickDownloadActivity.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/QuickDownloadActivity.kt). | **KEEP** | `WP 2.1` (Instagram link interceptor) |
| **10. Background Execution** | Promotes extraction to `dataSync` foreground service ([`DownloadService.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/DownloadService.kt)). | **KEEP** | Phase 1 baseline verified |
| **11. Error Dialogs** | Catches `YoutubeDLException` in [`App.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/App.kt) and routes stack trace to [`CrashReportActivity.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/CrashReportActivity.kt). | **KEEP** | Phase 1 baseline verified |
