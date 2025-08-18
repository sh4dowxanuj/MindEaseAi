package com.mindeaseai.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mindeaseai.BuildConfig
import android.util.Log

@HiltViewModel
class GeminiViewModel @Inject constructor(
    private val apiService: GeminiApiService
) : ViewModel() {
    private val geminiApiKey = BuildConfig.GEMINI_API_KEY
    private val _messages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val messages: StateFlow<List<Pair<String, String>>> = _messages
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun sendMessage(userMessage: String) {
        Log.d("GeminiViewModel", "sendMessage called with: $userMessage")
        Log.d("GeminiViewModel", "API Key configured: ${!geminiApiKey.isBlank()}")
        
        if (userMessage.isBlank()) {
            _error.value = "Please enter a message."
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                Log.d("GeminiViewModel", "Making API request...")
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = userMessage))))
                )
                val response = apiService.generateContent(request.copy())
                Log.d("GeminiViewModel", "API response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        Log.e("GeminiViewModel", "Response body is null")
                        _error.value = "No response from AI. Please try again."
                        return@launch
                    }
                    val aiText = body.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    Log.d("GeminiViewModel", "AI response text: $aiText")
                    
                    if (aiText.isNullOrBlank()) {
                        Log.e("GeminiViewModel", "AI text is null or blank")
                        _error.value = "AI did not return a valid response."
                        return@launch
                    }
                    val formatted = formatGeminiResponse(aiText)
                    _messages.value = _messages.value + (userMessage to formatted)
                    _error.value = null
                    Log.d("GeminiViewModel", "Message added successfully")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("GeminiViewModel", "API error: ${response.code()} - $errorMsg")
                    _error.value = when {
                        response.code() == 400 -> "Invalid request. Please check your message."
                        response.code() == 401 -> "API key is invalid. Please check your configuration."
                        response.code() == 403 -> "Access denied. Please check your API key permissions."
                        response.code() == 429 -> "Rate limit exceeded. Please try again later."
                        response.code() >= 500 -> "Server error. Please try again later."
                        else -> "API Error: ${response.code()}"
                    }
                }
        } catch (e: Exception) {
                Log.e("GeminiViewModel", "Exception occurred", e)
                _error.value = when {
            geminiApiKey.isBlank() -> "API key not configured. Please add your Gemini API key to local.properties or set it via environment and rebuild."
                    e.message?.contains("network", true) == true -> "Network error. Please check your connection."
                    else -> "Sorry, something went wrong. Please try again."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatGeminiResponse(response: String): String {
        // Warm, empathetic formatting
        return "${response}\n\nRemember, you are not alone. Take care of yourself."
    }
}
