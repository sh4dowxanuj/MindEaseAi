package com.mindeaseai.ui.mood

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerScreen(
    selectedMood: Int?,
    onMoodSelected: (Int, String) -> Unit,
    moodsHistory: List<com.mindeaseai.data.MoodEntry> = emptyList(),
    moodStats: Map<Int, Int> = emptyMap(),
    errorMessage: String? = null
) {
    val moods = listOf("😊", "😐", "😢", "😡", "😱")
    var note by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) snackbarHostState.showSnackbar(errorMessage)
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mood Tracker", style = MaterialTheme.typography.headlineSmall) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("How are you feeling today?", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            // Use a horizontally scrollable row so all emoji are always reachable on narrow screens
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(moods) { index, emoji ->
                    val selected = selectedMood == index
                    OutlinedButton(
                        onClick = { onMoodSelected(index, note) },
                        modifier = Modifier,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (selected) ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp) else ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text(emoji, style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add a note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Mood History", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                items(moodsHistory) { entry ->
                    val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(moods.getOrElse(entry.mood) { "?" }, style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(date, style = MaterialTheme.typography.bodySmall)
                        }
                        if (entry.note.isNotBlank()) {
                            Text(entry.note, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 32.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Mood Analytics", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                moods.forEachIndexed { idx, emoji ->
                    val count = moodStats[idx] ?: 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                        Text("$count", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
