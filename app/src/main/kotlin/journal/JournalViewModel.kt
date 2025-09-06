
package com.mindeaseai.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeaseai.data.JournalDao
import com.mindeaseai.data.JournalEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao
) : ViewModel() {
    
    init {
        loadJournals()
    }
    
    fun searchJournals(query: String) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { journalDao.searchJournals("%$query%") }
                _journals.value = result
            } catch (e: Exception) {
                android.util.Log.e(TAG, "searchJournals failed", e)
                _error.value = "Search failed."
            }
        }
    }

    fun filterJournalsByMood(mood: String?) {
        viewModelScope.launch {
            try {
                val all = withContext(Dispatchers.IO) { journalDao.getAllJournals() }
                _journals.value = if (mood.isNullOrEmpty()) all else all.filter { it.mood == mood }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "filterJournalsByMood failed", e)
                _error.value = "Filter failed."
            }
        }
    }
    private val _journalText = MutableStateFlow("")
    val journalText: StateFlow<String> = _journalText
    private val _journalMood = MutableStateFlow<String?>(null)
    val journalMood: StateFlow<String?> = _journalMood
    private val _journalSentiment = MutableStateFlow<Float?>(null)
    val journalSentiment: StateFlow<Float?> = _journalSentiment
    private val _journals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journals: StateFlow<List<JournalEntry>> = _journals
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateText(text: String) { _journalText.value = text }
    fun updateMood(mood: String?) { _journalMood.value = mood }
    fun updateSentiment(sentiment: Float?) { _journalSentiment.value = sentiment }

    fun clearError() {
        _error.value = null
    }

    fun saveJournal() {
        viewModelScope.launch {
            try {
                if (_journalText.value.isBlank()) {
                    _error.value = "Please write something before saving."
                    return@launch
                }
                withContext(Dispatchers.IO) {
                    journalDao.insertJournal(
                        JournalEntry(
                            text = _journalText.value,
                            timestamp = System.currentTimeMillis(),
                            mood = _journalMood.value,
                            sentiment = _journalSentiment.value
                        )
                    )
                }
                _journalText.value = ""
                _journalMood.value = null
                _journalSentiment.value = null
                _error.value = null
                loadJournals()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "saveJournal failed", e)
                _error.value = "Failed to save journal entry. Please try again."
            }
        }
    }

    fun loadJournals() {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) { journalDao.getAllJournals() }
                _journals.value = list
                _error.value = null
            } catch (e: Exception) {
                android.util.Log.e(TAG, "loadJournals failed", e)
                _error.value = "Failed to load journal entries."
            }
        }
    }

    companion object { private const val TAG = "JournalViewModel" }
}
