# MIGRATION GUIDE — Seal to InstaFlow

## 1. Package & Namespace Migration Steps

1. **Namespace Refactoring**:
   - Upstream: `com.junkfood.seal`
   - Target: `com.instaflow.app`
   - Update `app/build.gradle.kts` namespace and `applicationId`.

2. **Database Migration (`AppDatabase.kt`)**:
   - Increment Room schema version from `1` to `2`.
   - Add Migration `MIGRATION_1_2`:
     ```sql
     ALTER TABLE video_info ADD COLUMN mediaType TEXT NOT NULL DEFAULT 'REEL';
     ALTER TABLE video_info ADD COLUMN instagramUsername TEXT;
     ALTER TABLE video_info ADD COLUMN captionText TEXT;
     ```

3. **Koin Module Registration (`App.kt`)**:
   - Register `InstagramExtractor` and `DownloaderV2` implementations in Koin DSL.

4. **UI Route Mapping (`AppEntry.kt`)**:
   - Map `/home` to Instagram URL Input & Media Preview page.
   - Replace `/playlist` routes with `/carousel_picker`.
