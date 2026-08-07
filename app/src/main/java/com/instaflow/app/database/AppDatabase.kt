package com.instaflow.app.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.instaflow.app.database.objects.AccountProfile
import com.instaflow.app.database.objects.CommandTemplate
import com.instaflow.app.database.objects.DownloadedVideoInfo
import com.instaflow.app.database.objects.OptionShortcut

@Database(
    entities =
        [
            DownloadedVideoInfo::class,
            CommandTemplate::class,
            AccountProfile::class,
            OptionShortcut::class,
        ],
    version = 7,
    autoMigrations =
        [
            AutoMigration(from = 1, to = 2),
            AutoMigration(from = 2, to = 3),
            AutoMigration(from = 3, to = 4),
            AutoMigration(from = 4, to = 5),
            AutoMigration(from = 5, to = 6),
            AutoMigration(from = 6, to = 7, spec = AppDatabase.RenameCookieTable::class),
        ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoInfoDao(): VideoInfoDao

    @RenameTable(fromTableName = "CookieProfile", toTableName = "AccountProfile")
    class RenameCookieTable : AutoMigrationSpec
}
