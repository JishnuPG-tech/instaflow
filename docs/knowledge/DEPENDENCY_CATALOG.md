# DEPENDENCY CATALOG — Version Catalog (`libs.versions.toml`)

## Dependency: `youtubedl-android`
- **Location**: [`gradle/libs.versions.toml`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/gradle/libs.versions.toml) (`io.github.junkfood02.youtubedl-android`)
- **Purpose**: Bundles native `yt-dlp`, `ffmpeg`, and `aria2c` executables with Python runtime.
- **Called by**: `Downloader.kt`, `DownloaderV2Impl.kt`
- **Depends on**: Native C++ / JNI
- **Thread**: `Dispatchers.IO`
- **Decision**: KEEP
- **Reason**: Stable, production-grade native binary wrapper.
- **Future modifications**: Keep pinned to latest upstream release.

---

## Dependency: `koin-android`
- **Location**: [`gradle/libs.versions.toml`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/gradle/libs.versions.toml) (`io.insert-koin:koin-android:4.0.0`)
- **Purpose**: Pragmatic dependency injection.
- **Called by**: `App.kt`
- **Depends on**: Kotlin Core
- **Thread**: Main
- **Decision**: KEEP
- **Reason**: Zero annotation processor compile-time overhead.
- **Future modifications**: Register new Instagram extraction modules.
