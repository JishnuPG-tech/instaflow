package com.instasave.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.instasave.app.core.database.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM download_history ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download_history WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDownloads(query: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("DELETE FROM download_history WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("DELETE FROM download_history")
    suspend fun clearHistory()
}
