package com.mindeaseai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JournalDao {

    @Insert
    suspend fun insertJournal(journal: JournalEntry)

    @Query("SELECT * FROM journal ORDER BY timestamp DESC")
    suspend fun getAllJournals(): List<JournalEntry>

    @Query("SELECT * FROM journal WHERE text LIKE :query OR mood LIKE :query ORDER BY timestamp DESC")
    suspend fun searchJournals(query: String): List<JournalEntry>
}
