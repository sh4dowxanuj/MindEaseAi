package com.mindeaseai.ui.ai

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindeaseai.gemini.GeminiViewModel
import com.mindeaseai.ui.ai.AIChatScreen


import androidx.hilt.navigation.compose.hiltViewModel
import com.mindeaseai.auth.AuthViewModel

@Composable
fun AIChatFlow(
    userName: String? = null,
    onLogout: (() -> Unit)? = null
) {
    val geminiViewModel: GeminiViewModel = hiltViewModel()
    val messages by geminiViewModel.messages.collectAsState()
    val isLoading by geminiViewModel.isLoading.collectAsState()
    val error by geminiViewModel.error.collectAsState()

    // Show greeting if chat is empty
    val showGreeting = messages.isEmpty()

    AIChatScreen(
        messages = messages,
        onSend = { geminiViewModel.sendMessage(it) },
        isLoading = isLoading,
        errorMessage = error,
        showGreeting = showGreeting,
        userName = userName,
        onLogout = onLogout
    )
}
