package com.mindeaseai.di

import android.content.Context
import androidx.room.Room
import com.mindeaseai.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "mindeaseai-db")
            .fallbackToDestructiveMigration()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    android.util.Log.d("DatabaseModule", "Database created successfully")
                }
                
                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onOpen(db)
                    android.util.Log.d("DatabaseModule", "Database opened successfully")
                }
            })
            .build()

    @Provides
    fun provideMoodDao(db: AppDatabase) = db.moodDao()

    @Provides
    fun provideJournalDao(db: AppDatabase) = db.journalDao()
}
