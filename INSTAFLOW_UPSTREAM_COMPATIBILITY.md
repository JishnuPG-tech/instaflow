# INSTAFLOW UPSTREAM COMPATIBILITY & MERGE STRATEGY

## 1. Upstream Sync Philosophy

To ensure InstaFlow can effortlessly pull security fixes, dependency updates, and engine optimizations from upstream [JunkFood02/Seal](https://github.com/JunkFood02/Seal), core infrastructure files must retain 100% interface compatibility.

---

## 2. Upstream Sync Rules

1. **Native Libraries (`youtubedl-android`)**:
   - Keep upstream dependency declarations in [`gradle/libs.versions.toml`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/gradle/libs.versions.toml) (`io.github.junkfood02.youtubedl-android`).
   - Do not modify JNI bridge code or C++ binary wrappers.

2. **Foreground Service (`DownloadService.kt`)**:
   - Maintain method signatures in [`DownloadService.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/DownloadService.kt).
   - Any foreground service bugfixes released in upstream Seal can be merged via `git merge upstream/main` without merge conflicts.

3. **Room Database Core (`AppDatabase.kt`)**:
   - Preserve existing table structures (`video_info`, `cookie_profile`, `command_template`).
   - Add new columns to `video_info` using Room `MIGRATION_X_Y` scripts rather than altering base entity fields.

4. **Dynamic Material 3 Color Module (`:color`)**:
   - Do not touch the `:color` library code. Keep as an independent Gradle subproject.

---

## 3. Git Branch & Remote Tracking Setup

```bash
# Add upstream Seal tracking remote
git remote add upstream https://github.com/JunkFood02/Seal.git

# Pull upstream infrastructure updates
git fetch upstream main
git merge upstream/main --strategy-option=ours
```
