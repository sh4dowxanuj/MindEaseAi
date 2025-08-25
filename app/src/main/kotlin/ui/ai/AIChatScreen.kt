package com.mindeaseai.ui.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindeaseai.ui.ai.AIChatLog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import android.util.Log

import com.mindeaseai.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    messages: List<Pair<String, String>>,
    onSend: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    showGreeting: Boolean = false,
    userName: String? = null
) {
    var userInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var lastUserInput by remember { mutableStateOf("") }

    Log.d("AIChatScreen", "Messages: ${messages.size}, isLoading: $isLoading, error: $errorMessage, showGreeting: $showGreeting")

    // Show error in Snackbar with retry action
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            Log.d("AIChatScreen", "Showing error snackbar: $errorMessage")
            snackbarHostState.showSnackbar(
                message = if (BuildConfig.DEBUG) errorMessage!! else "Sorry, something went wrong. Please try again.",
                actionLabel = if (!isLoading && lastUserInput.isNotBlank()) "Retry" else null
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini Assistant", style = MaterialTheme.typography.headlineSmall) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Chat log takes up most of the space
            AIChatLog(
                messages = messages,
                showGreeting = showGreeting,
                userName = userName,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Input area at the bottom
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("Type your message...") },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                Log.d("AIChatScreen", "Sending message: $userInput")
                                lastUserInput = userInput
                                onSend(userInput)
                                userInput = ""
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
            
            // Retry button if error and lastUserInput is available
            if (!errorMessage.isNullOrBlank() && !isLoading && lastUserInput.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSend(lastUserInput)
                        userInput = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry Last Message")
                }
            }
        }
    }
}
