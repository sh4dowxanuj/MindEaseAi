package com.mindeaseai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: Int,
    val timestamp: Long,
    val note: String = ""
)
