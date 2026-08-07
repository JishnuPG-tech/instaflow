# DAO Reference — `VideoInfoDao`

## 1. Interface Overview

[`VideoInfoDao.kt`](file:///C:/Users/JISHNU%20PG/Music/InstaFlow/InstaFlow/app/src/main/java/com/instaflow/app/database/VideoInfoDao.kt) defines all Room database access methods for queries, insertions, updates, and deletions across history items, account profiles, and command templates.

---

## 2. API Method Signature Mapping

```kotlin
@Dao
interface VideoInfoDao {
    @Query("select * from DownloadedVideoInfo")
    fun getDownloadHistoryFlow(): Flow<List<DownloadedVideoInfo>>

    @Insert suspend fun insert(info: DownloadedVideoInfo)

    @Delete suspend fun deleteInfo(vararg info: DownloadedVideoInfo)

    @Query("select * from AccountProfile")
    fun getAccountProfileFlow(): Flow<List<AccountProfile>>

    @Insert suspend fun insertAccountProfile(accountProfile: AccountProfile)

    @Delete suspend fun deleteAccountProfile(accountProfile: AccountProfile)
}
```
