package com.mindeaseai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String, // Can be HTML or Markdown for rich text
    val timestamp: Long,
    val mood: String? = null, // e.g., "happy", "sad", emoji, etc.
    val sentiment: Float? = null // Sentiment score (-1 to 1)
)
