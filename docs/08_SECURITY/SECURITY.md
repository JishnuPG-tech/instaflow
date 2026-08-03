# Security Architecture — Storage Access Framework & Network Security

## 1. Storage Access Framework (SAF) Compliance

- **Scoped Storage**: Fully complies with Android 10+ (API 29+) and Android 11+ (API 30+) scoped storage requirements.
- **`ACTION_OPEN_DOCUMENT_TREE`**: Requests explicit user permission to write into chosen directory subfolders without requiring dangerous legacy `MANAGE_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE` broad permissions.

---

## 2. Network Security & Credential Hygiene

- **No Remote Credentials**: Seal contains no backend servers, analytics tracking, or user credential collection.
- **Cookie Non-Persistence**: User cookies remain 100% stored on-device in app private memory and are never transmitted to third parties except the origin media server during `yt-dlp` requests.
- **ProGuard / R8 Obfuscation**: Release builds enable `isMinifyEnabled = true` and `isShrinkResources = true` with rules defined in [`proguard-rules.pro`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/proguard-rules.pro).
