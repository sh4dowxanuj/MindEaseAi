package com.mindeaseai.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeaseai.data.MoodDao
import com.mindeaseai.data.MoodEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val moodDao: MoodDao
) : ViewModel() {

    init {
        loadMoods()
    }

    private val _selectedMood = MutableStateFlow<Int?>(null)
    val selectedMood: StateFlow<Int?> = _selectedMood
    private val _moods = MutableStateFlow<List<MoodEntry>>(emptyList())
    val moods: StateFlow<List<MoodEntry>> = _moods
    private val _moodNotes = MutableStateFlow("")
    val moodNotes: StateFlow<String> = _moodNotes
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun selectMood(mood: Int, note: String = "") {
        if (mood < 0) {
            _error.value = "Please select a valid mood."
            return
        }
        _selectedMood.value = mood
        _moodNotes.value = note
        viewModelScope.launch {
            try {
                moodDao.insertMood(MoodEntry(mood = mood, timestamp = System.currentTimeMillis(), note = note))
                _error.value = null
                loadMoods()
            } catch (e: Exception) {
                _error.value = "Failed to save mood entry. Please try again."
            }
        }
    }

    fun setNote(note: String) {
        _moodNotes.value = note
    }

    fun clearError() {
        _error.value = null
    }

    fun loadMoods() {
        viewModelScope.launch {
            try {
                _moods.value = moodDao.getAllMoods()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load mood history."
            }
        }
    }

    fun getMoodStats(): Map<Int, Int> {
        // Ensure moods are loaded before calculating stats
        if (_moods.value.isEmpty()) {
            loadMoods()
        }
        return _moods.value.groupingBy { it.mood }.eachCount()
    }
}
