# Room Database Configuration — `AppDatabase`

## 1. Database Specifications

- **Database Class**: [`AppDatabase.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/database/AppDatabase.kt)
- **Database Name**: `video_info_database`
- **Room Version**: `2.6.1` (compiled via KSP)
- **Schema Export Path**: `app/schemas`

---

## 2. Entity Register

`AppDatabase` manages 4 database tables:

1. **`DownloadedVideoInfo`**: Download history records.
2. **`CommandTemplate`**: User-defined custom `yt-dlp` CLI command templates.
3. **`CookieProfile`**: Exported Netscape format domain cookies.
4. **`OptionShortcut`**: Quick format preset shortcuts.

---

## 3. Schema & Migrations

- Configured with `exportSchema = true` in `@Database` annotation.
- Automatic destructive fallback or manual `Migration` definitions handle database updates across app releases without user data loss.
