package com.instasave.app.core.data.repository

import com.instasave.app.core.database.dao.DownloadDao
import com.instasave.app.core.database.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadHistoryRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    fun getDownloadHistory(query: String = ""): Flow<List<DownloadEntity>> {
        return if (query.isBlank()) {
            downloadDao.getAllDownloads()
        } else {
            downloadDao.searchDownloads(query)
        }
    }

    suspend fun saveDownload(download: DownloadEntity) {
        downloadDao.insertDownload(download)
    }

    suspend fun deleteDownload(id: String) {
        downloadDao.deleteDownload(id)
    }

    suspend fun clearAllHistory() {
        downloadDao.clearHistory()
    }
}
