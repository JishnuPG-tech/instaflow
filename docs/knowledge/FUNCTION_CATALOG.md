# FUNCTION CATALOG — Core Function & API Register

## Function: `YoutubeDL.getInstance().execute()`
- **Location**: Native JNI bridge in `youtubedl-android`
- **Purpose**: Spawns native `yt-dlp` Python process with CLI options.
- **Called by**: `Downloader.kt`, `DownloaderV2Impl.kt`
- **Depends on**: Compiled C++ binaries (`libyoutubedl.so`, `libffmpeg.so`, `libaria2c.so`)
- **Thread**: `Dispatchers.IO`
- **Decision**: KEEP
- **Reason**: Tested, production-grade native binary wrapper.
- **Future modifications**: Pass Instagram cookie profiles and format option arrays.

---

## Function: `DownloadUtil.fetchVideoInfoFromUrl()`
- **Location**: [`app/src/main/java/com/junkfood/seal/util/DownloadUtil.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/util/DownloadUtil.kt)
- **Purpose**: Fetches metadata JSON via `yt-dlp -j`.
- **Called by**: `DownloadDialogViewModel`
- **Depends on**: `youtubedl-android`
- **Thread**: `Dispatchers.IO`
- **Decision**: MODIFY
- **Reason**: Needs to parse Instagram specific JSON fields (Carousel items, GraphQL media URLs).
- **Future modifications**: Add fallback GraphQL API parser when `yt-dlp` rate-limits.
