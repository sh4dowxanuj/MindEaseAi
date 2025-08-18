package com.mindeaseai.ui.mood

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindeaseai.mood.MoodViewModel
import com.mindeaseai.ui.mood.MoodTrackerScreen

@Composable
fun MoodTrackerFlow() {
    val moodViewModel: MoodViewModel = hiltViewModel()
    val selectedMood by moodViewModel.selectedMood.collectAsState()
    val moodsHistory by moodViewModel.moods.collectAsState()
    val moodStats = moodViewModel.getMoodStats()
    val errorMessage by moodViewModel.error.collectAsState()

    MoodTrackerScreen(
        selectedMood = selectedMood,
        onMoodSelected = { mood, note -> moodViewModel.selectMood(mood, note) },
        moodsHistory = moodsHistory,
        moodStats = moodStats,
        errorMessage = errorMessage
    )
}
