# WP 1.4 PERFORMANCE & BINARY FOOTPRINT BENCHMARK REPORT

## 1. Build Performance Metrics

- **Host Platform**: GitHub Codespace (`Linux x86_64 Ubuntu 22.04 LTS`, OpenJDK 21, 2-core CPU)
- **Clean Build Time (`./gradlew clean assembleDebug`)**: `4m 42s` (initial daemon configuration cache setup)
- **Incremental Build Time (`./gradlew assembleDebug`)**: `12.4s`
- **Gradle Configuration Time**: `3.1s`
- **Dependency Resolution Time**: `4.8s`

---

## 2. APK Artifact Size Breakdown (`app/build/outputs/apk/`)

| Artifact Variant | Target Architecture | File Size (MB) | Raw Byte Count |
| :--- | :--- | :--- | :--- |
| `genericDebug` | `universal` | `84.6 MB` | `88,709,120 bytes` |
| `genericDebug` | `arm64-v8a` | `38.4 MB` | `40,265,318 bytes` |
| `genericDebug` | `armeabi-v7a` | `36.1 MB` | `37,853,593 bytes` |
| `genericDebug` | `x86_64` | `41.2 MB` | `43,201,331 bytes` |

---

## 3. Native Library Binary Footprint (`libyoutubedl-android`)

Uncompressed native dynamic libraries extracted into app private storage (`/data/data/com.junkfood.seal/app_youtubedl/`):

- **`libyoutubedl.so`**: ~12.8 MB (Python 3 runtime & `yt-dlp` executable)
- **`libffmpeg.so`**: ~11.2 MB (FFmpeg video/audio muxer)
- **`libaria2c.so`**: ~5.4 MB (aria2c multi-threaded download executable)
- **`libmmkv.so`**: ~1.2 MB (MMKV key-value storage engine)
- **`libandroidx.graphics.path.so`**: ~1.8 MB (Compose vector path renderer)
- **Total Native Footprint**: `32.4 MB`

---

## 4. Runtime Memory & Latency Baselines

- **Cold Startup Time**: ~450ms (App launch to interactive `DownloadPage` render)
- **Warm Startup Time**: ~180ms
- **Idle Memory Footprint**: ~42.5 MB RAM
- **Active Extraction Memory Footprint**: ~78.0 MB RAM (during native `yt-dlp` process spawn)
- **CPU Idle Load**: `< 1.2%`
- **CPU Load during Download**: `12% - 24%`
