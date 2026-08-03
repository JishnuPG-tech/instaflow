package com.instasave.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.instasave.app.core.database.dao.DownloadDao
import com.instasave.app.core.database.entity.DownloadEntity

@Database(
    entities = [DownloadEntity::class],
    version = 1,
    exportSchema = false
)
abstract class InstaSaveDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
