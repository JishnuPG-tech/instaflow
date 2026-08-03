# Project Overview — JunkFood02/Seal

## 1. Executive Summary

**Seal** is a modern, high-performance, open-source Android video/audio downloader application created by **JunkFood02**. It provides an intuitive, Material Design 3 user interface built entirely with Jetpack Compose, wrapping the powerful command-line media extraction capabilities of `yt-dlp`, `ffmpeg`, and `aria2c`.

---

## 2. Technical Identity & Attributes

| Attribute | Value / Technology |
| :--- | :--- |
| **Application ID** | `com.junkfood.seal` (Debug: `com.junkfood.seal.debug`, Preview: `com.junkfood.seal.preview`) |
| **License** | GNU General Public License v3.0 (`GPLv3`) |
| **Compile SDK / Target SDK** | `35` (Android 15) |
| **Minimum SDK** | `24` (Android 7.0 Nougat) |
| **Kotlin Version** | `2.0.20` |
| **Java Toolchain** | JDK `21` |
| **UI Framework** | Jetpack Compose + Material Design 3 + Dynamic Colors |
| **Dependency Injection** | Koin `4.0.0` (`koin-android`, `koin-androidx-compose`) |
| **Database** | Room `2.6.1` with KSP (`VideoInfoDao`, SQLite) |
| **Key-Value Storage** | MMKV `1.3.12` |
| **Media Extraction** | `youtubedl-android` `0.17.3` (wraps `yt-dlp`, `ffmpeg`, `aria2c`) |
| **Network Engine** | OkHttp `5.0.0-alpha.10` |
| **Image Loading** | Coil `2.5.0` (`coil-compose`) |

---

## 3. Core Architectural Highlights

1. **Single-Activity Architecture**: The application runs within [`MainActivity`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/MainActivity.kt), utilizing Navigation Compose for all destination routes.
2. **Foreground Service Execution**: Long-running extraction and downloading tasks run via [`DownloadService`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/DownloadService.kt), displaying notification progress updates.
3. **Pluggable Download Engines**: Supports legacy execution ([`Downloader.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/Downloader.kt)) and structured async task queue management ([`DownloaderV2.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/download/DownloaderV2.kt)).
4. **Storage Access Framework (SAF)**: Ensures full compliance with Android 10+ scoped storage rules, allowing direct writes to user-selected directories.
5. **Cookie Management**: Features Netscape format cookie profile creation and DB backup to enable authenticated media downloads (e.g. age-gated YouTube or private media).

---

## 4. Upstream Adaptation Context (InstaFlow Basis)

Seal serves as the foundation for **InstaFlow**. While Seal handles generic multi-platform media downloads across 1000+ sites, InstaFlow specializes its UI, media model, and format selectors specifically for Instagram content (Reels, Carousels, Posts, Stories).
