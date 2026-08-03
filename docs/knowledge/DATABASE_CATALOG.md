# DATABASE CATALOG — Room DB Entities & Tables

## Database: `AppDatabase`
- **Location**: [`app/src/main/java/com/junkfood/seal/database/AppDatabase.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/database/AppDatabase.kt)
- **Purpose**: Main Room SQLite database instance (`video_info_database`).
- **Called by**: Koin DI / ViewModels
- **Depends on**: Room Runtime 2.6.1
- **Thread**: `Dispatchers.IO`
- **Decision**: KEEP & MODIFY
- **Reason**: Stable SQLite persistence layer.
- **Future modifications**: Add Migration `MIGRATION_1_2` for Instagram metadata columns.

---

## Entity: `DownloadedVideoInfo`
- **Location**: [`app/src/main/java/com/junkfood/seal/database/objects/DownloadedVideoInfo.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/database/objects/DownloadedVideoInfo.kt)
- **Purpose**: History table record.
- **Called by**: `VideoInfoDao`
- **Depends on**: Room Annotations
- **Thread**: Data Model
- **Decision**: MODIFY
- **Reason**: Needs extra columns for Instagram media type, username, and post caption.
- **Future modifications**: Add `@ColumnInfo name = "mediaType"`, `instagramUsername`, `captionText`.
