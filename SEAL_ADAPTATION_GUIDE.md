# SEAL ADAPTATION GUIDE — InstaFlow Evolution Strategy

## 1. Overview & Purpose

This document outlines the strategic engineering blueprint for adapting the upstream **Seal** (`com.junkfood.seal`) codebase into **InstaFlow** (`com.instaflow.app`), an Instagram-specialized media downloader and management tool.

The adaptation principle is **Preserve Mature Infrastructure, Specialize User Workflows**.

---

## 2. Categorization Rules (KEEP / MODIFY / REPLACE / REMOVE / EXTRACT / GENERALIZE)

Every subsystem in the Seal repository is classified into one of 6 operational categories:

- **KEEP**: Preserve unchanged (or minimal branding updates). Subsystems that are mature, production-tested, and platform-agnostic.
- **MODIFY**: Enhance existing logic to support Instagram-specific metadata, media types, or UX enhancements.
- **REPLACE**: Completely substitute generic Seal UI/UX components with Instagram-native alternatives (e.g. Playlist selector -> Carousel Picker).
- **REMOVE**: Strip out features irrelevant to Instagram (e.g. SponsorBlock, Subtitles, YouTube-specific format selectors).
- **EXTRACT**: Isolate core features into independent modules for cleaner architectural boundaries.
- **GENERALIZE**: Abstract concrete implementations into reusable interfaces (e.g. pluggable `MediaExtractor` interface).

---

## 3. Subsystem Adaptation Breakdown

### A. Download Engine (`com.junkfood.seal.download`, `Downloader.kt`)
- **Classification**: **MODIFY** & **GENERALIZE**
- **Evidence in Code**: [`Downloader.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/Downloader.kt) is currently a monolithic `object` tightly coupled to generic `yt-dlp` CLI format options (`-f`, `--recode-video`).
- **Adaptation Rationale**: Generalize the download engine behind a pluggable `MediaExtractor` interface (`GraphQLExtractor`, `BackendExtractor`, `YtDlpExtractor`). Instagram media endpoints fail frequently; fallback extraction paths are mandatory.

### B. Storage & Database (`AppDatabase.kt`, `VideoInfoDao.kt`, `DownloadedVideoInfo.kt`)
- **Classification**: **KEEP** & **MODIFY**
- **Evidence in Code**: [`AppDatabase.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/database/AppDatabase.kt) provides a stable Room DB foundation. [`DownloadedVideoInfo.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/database/objects/DownloadedVideoInfo.kt) contains generic fields (`videoTitle`, `videoAuthor`, `videoUrl`).
- **Adaptation Rationale**: Keep Room DB infrastructure. Modify `DownloadedVideoInfo` entity schema to include Instagram-specific fields (`mediaType`: Carousel/Reel/Story/Post, `instagramUsername`, `captionText`, `likeCount`).

### C. Preferences & Storage Settings (`PreferenceUtil.kt`, `MMKV`)
- **Classification**: **KEEP**
- **Evidence in Code**: [`PreferenceUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/PreferenceUtil.kt) uses high-speed binary MMKV storage (`com.tencent:mmkv`).
- **Adaptation Rationale**: Keep MMKV without modifications to core storage logic. Strip out YouTube-specific preference keys (`sponsorblock`, `subtitle_language`).

### D. Foreground Service & Notifications (`DownloadService.kt`, `NotificationUtil.kt`)
- **Classification**: **KEEP** & **MODIFY**
- **Evidence in Code**: [`DownloadService.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/DownloadService.kt) provides Android 14+ compatible `dataSync` foreground execution.
- **Adaptation Rationale**: Keep service architecture intact. Modify notification copy and icons to reflect Instagram download progress and multi-item Carousel batch progress.

### E. Format Picker & Playlist UI (`DownloadSettingsDialog.kt`, `PlaylistSelectionDialog.kt`)
- **Classification**: **REPLACE**
- **Evidence in Code**: [`DownloadSettingsDialog.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/ui/page/download/DownloadSettingsDialog.kt) presents generic audio/video codec options (MP4, WEBM, MP3, OPUS, 1080p, 720p).
- **Adaptation Rationale**: Instagram media does not offer separate audio/video codec options or YouTube resolution ladders. Replace with an Instagram Media Previewer & Carousel Picker allowing individual item selection from mixed posts.

### F. Account Management (`AccountsViewModel.kt`, `AccountProfile.kt`)
- **Classification**: **KEEP** & **MODIFY**
- **Evidence in Code**: [`AccountProfile.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/instaflow/app/database/objects/AccountProfile.kt) stores Netscape formatted session account text.
- **Adaptation Rationale**: Keep Netscape cookie injection engine. Modify UI to provide an inline Instagram Webview login helper to automatically grab session accounts (`sessionid`, `ds_user_id`, `csrftoken`).
