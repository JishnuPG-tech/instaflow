package com.instasave.app.di

import android.content.Context
import androidx.room.Room
import com.instasave.app.core.database.InstaSaveDatabase
import com.instasave.app.core.database.dao.DownloadDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideInstaSaveDatabase(
        @ApplicationContext context: Context
    ): InstaSaveDatabase {
        return Room.databaseBuilder(
            context,
            InstaSaveDatabase::class.java,
            "instasave.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: InstaSaveDatabase): DownloadDao {
        return database.downloadDao()
    }
}
