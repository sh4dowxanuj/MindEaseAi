package com.mindeaseai.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MoodEntry::class, JournalEntry::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
}
