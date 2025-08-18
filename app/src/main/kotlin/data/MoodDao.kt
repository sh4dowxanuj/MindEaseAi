package com.mindeaseai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoodDao {
    @Insert
    suspend fun insertMood(mood: MoodEntry)

    @Query("SELECT * FROM mood ORDER BY timestamp DESC")
    suspend fun getAllMoods(): List<MoodEntry>
}
