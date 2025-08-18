package com.mindeaseai.ui.journal

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindeaseai.data.JournalEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    journalText: String,
    onTextChange: (String) -> Unit,
    journalMood: String?,
    onMoodChange: (String?) -> Unit,
    journalSentiment: Float?,
    onSentimentChange: (Float?) -> Unit,
    onSave: () -> Unit,
    errorMessage: String? = null,
    journals: List<JournalEntry> = emptyList(),
    onSearch: (String) -> Unit = {},
    onFilterMood: (String?) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) snackbarHostState.showSnackbar(errorMessage)
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Journal", style = MaterialTheme.typography.headlineSmall) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = journalText,
                        onValueChange = onTextChange,
                        label = { Text("Write your thoughts...") },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        singleLine = false,
                        maxLines = 10
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Mood selection (simple emoji row)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val moods = listOf("😊", "😐", "😢", "😡", "😱")
                        moods.forEach { mood ->
                            FilterChip(
                                selected = journalMood == mood,
                                onClick = { onMoodChange(mood) },
                                label = { Text(mood) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Sentiment slider
                    Text("Sentiment: ${journalSentiment ?: 0f}")
                    Slider(
                        value = journalSentiment ?: 0f,
                        onValueChange = { onSentimentChange(it) },
                        valueRange = -1f..1f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Text("Save")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearch(it)
                        },
                        label = { Text("Search journal entries...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Mood filter
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val moods = listOf("All", "😊", "😐", "😢", "😡", "😱")
                        moods.forEach { mood ->
                            FilterChip(
                                selected = (mood == "All" && selectedMood == null) || (mood == selectedMood),
                                onClick = {
                                    selectedMood = if (mood == "All") null else mood
                                    onFilterMood(selectedMood)
                                },
                                label = { Text(mood) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Journal entries list
                    Text("Entries:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(journals) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(entry.text, style = MaterialTheme.typography.bodyLarge)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(entry.mood ?: "", style = MaterialTheme.typography.bodySmall)
                                        Text(entry.timestamp.toString(), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
