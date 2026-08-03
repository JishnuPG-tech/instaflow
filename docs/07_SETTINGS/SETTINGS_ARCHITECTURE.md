# Settings Architecture — MMKV Storage Integration

## 1. Subsystem Overview

Seal uses **MMKV 1.3.12** by Tencent for non-volatile key-value storage instead of Android SharedPreferences or Jetpack DataStore.

- **Fast Binary Storage**: Uses memory-mapped files (`mmap`) for instant multi-process read/write operations without disk I/O blocking on the main thread.
- **Initialization**: Called in [`App.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/App.kt) via `MMKV.initialize(this)`.

---

## 2. Preference Categories

1. **General**: Theme mode (Light, Dark, System, Pure Black), Language locale code, Dynamic Color toggle.
2. **Download & Format**: Default video resolution, audio format, aria2 multi-connection count, video section clipping defaults.
3. **Directory**: Storage Access Framework tree URI, custom filename template strings (e.g. `%(title)s [%(id)s].%(ext)s`).
4. **Network & Cookies**: Active cookie profile ID, custom User-Agent, HTTP proxy server URL.
