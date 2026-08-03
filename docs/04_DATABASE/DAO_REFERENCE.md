# DAO Reference — `VideoInfoDao`

## 1. Interface Overview

[`VideoInfoDao.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/junkfood/seal/database/VideoInfoDao.kt) defines all Room database access methods for queries, insertions, updates, and deletions across history items, cookie profiles, and command templates.

---

## 2. API Method Signature Mapping

```kotlin
@Dao
interface VideoInfoDao {
    @Query("SELECT * FROM video_info ORDER BY id DESC")
    fun getAllVideoInfo(): Flow<List<DownloadedVideoInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoInfo(videoInfo: DownloadedVideoInfo): Long

    @Delete
    suspend fun deleteVideoInfo(videoInfo: DownloadedVideoInfo)

    @Query("DELETE FROM video_info")
    suspend fun deleteAll()

    @Query("SELECT * FROM cookie_profile")
    fun getAllCookieProfiles(): Flow<List<CookieProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCookieProfile(cookieProfile: CookieProfile)

    @Delete
    suspend fun deleteCookieProfile(cookieProfile: CookieProfile)
}
```
