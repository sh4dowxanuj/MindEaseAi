package com.mindeaseai.ui.journal

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindeaseai.journal.JournalViewModel
import com.mindeaseai.ui.journal.JournalScreen

@Composable
fun JournalFlow() {
    val journalViewModel: JournalViewModel = hiltViewModel()
    val journalText by journalViewModel.journalText.collectAsState()
    val journalMood by journalViewModel.journalMood.collectAsState()
    val journalSentiment by journalViewModel.journalSentiment.collectAsState()
    val errorMessage by journalViewModel.error.collectAsState()

    JournalScreen(
        journalText = journalText,
        onTextChange = { journalViewModel.updateText(it) },
        journalMood = journalMood,
        onMoodChange = { journalViewModel.updateMood(it) },
        journalSentiment = journalSentiment,
        onSentimentChange = { journalViewModel.updateSentiment(it) },
        onSave = { journalViewModel.saveJournal() },
        errorMessage = errorMessage,
        journals = journalViewModel.journals.collectAsState().value,
        onSearch = { journalViewModel.searchJournals(it) },
        onFilterMood = { journalViewModel.filterJournalsByMood(it) }
    )
}
