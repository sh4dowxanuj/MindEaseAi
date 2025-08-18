package com.mindeaseai.tips

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.mindeaseai.ui.tips.DailyTip
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import javax.inject.Inject
import com.mindeaseai.gemini.GeminiApiService
import com.mindeaseai.gemini.GeminiRequest
import com.mindeaseai.gemini.Content
import com.mindeaseai.gemini.Part
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class TipsViewModel @Inject constructor(
    app: Application,
    private val geminiApiService: GeminiApiService
) : AndroidViewModel(app) {
    private val _tips = MutableStateFlow<List<DailyTip>>(emptyList())
    val tips: StateFlow<List<DailyTip>> = _tips
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _aiGeneratedTips = MutableStateFlow<List<DailyTip>>(emptyList())
    val aiGeneratedTips: StateFlow<List<DailyTip>> = _aiGeneratedTips
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions

    init {
        loadTips()
        generateSuggestions()
    }

    fun loadTips() {
        Log.d("TipsViewModel", "loadTips called")
        viewModelScope.launch {
            val tipsList = mutableListOf<DailyTip>()
            try {
                val assetManager = getApplication<Application>().assets
                assetManager.open("daily_tips.json").use { inputStream ->
                    val json = inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonArray = JSONArray(json)
                    for (i in 0 until jsonArray.length()) {
                        val tipText = jsonArray.getString(i)
                        if (tipText.isNotBlank()) {
                            tipsList.add(
                                DailyTip(
                                    id = i + 1,
                                    text = tipText.trim(),
                                    category = "General"
                                )
                            )
                        }
                    }
                }
                if (tipsList.isNotEmpty()) {
                    _tips.value = tipsList
                    _error.value = null
                    Log.d("TipsViewModel", "Tips loaded: ${tipsList.size}")
                } else {
                    _error.value = "No tips found in the data file."
                    _tips.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("TipsViewModel", "Error loading tips", e)
                _error.value = "Failed to load daily tips. Please try again."
                _tips.value = emptyList()
            }
        }
    }

    private fun generateSuggestions() {
        _suggestions.value = listOf(
            "I'm feeling stressed today",
            "Help me sleep better",
            "I need motivation",
            "I'm feeling anxious",
            "I want to be more mindful",
            "I need self-care ideas",
            "Help me with work-life balance",
            "I'm feeling overwhelmed",
            "I want to improve my mood",
            "I need relaxation techniques"
        )
    }

    fun generatePersonalizedTip(userMood: String? = null, context: String? = null) {
        if (_isLoading.value) {
            Log.d("TipsViewModel", "Already loading, skipping request")
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val currentDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                
                val timeContext = when {
                    currentHour < 12 -> "morning"
                    currentHour < 17 -> "afternoon"
                    currentHour < 21 -> "evening"
                    else -> "night"
                }
                
                val prompt = buildString {
                    append("Generate a personalized mental health tip that feels warm and supportive. ")
                    append("Current time: $currentTime ($timeContext), Day: $currentDay. ")
                    if (!userMood.isNullOrBlank()) {
                        append("User's current mood: $userMood. ")
                    }
                    if (!context.isNullOrBlank()) {
                        append("Context: $context. ")
                    }
                    append("Make the tip specific, actionable, and empathetic. ")
                    append("Keep it under 80 words and make it feel like a caring friend is giving advice. ")
                    append("Format the response as just the tip text, no additional formatting.")
                }
                
                Log.d("TipsViewModel", "Generating personalized tip with prompt: $prompt")
                
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                val response = geminiApiService.generateContent(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val aiText = body.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!aiText.isNullOrBlank()) {
                            val newTip = DailyTip(
                                id = System.currentTimeMillis().toInt(),
                                text = aiText.trim(),
                                category = "AI Generated"
                            )
                            _aiGeneratedTips.value = _aiGeneratedTips.value + newTip
                            Log.d("TipsViewModel", "Generated tip: $aiText")
                        } else {
                            _error.value = "AI did not return a valid tip."
                        }
                    } else {
                        _error.value = "No response from AI. Please try again."
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("TipsViewModel", "API error: ${response.code()} - $errorMsg")
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
                Log.e("TipsViewModel", "Error generating tip", e)
                _error.value = when {
                    e.message?.contains("GEMINI_API_KEY") == true -> "API key not configured. Please add your Gemini API key to gradle.properties"
                    e.message?.contains("network", true) == true -> "Network error. Please check your connection."
                    else -> "Failed to generate personalized tip. Please try again."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateTipsByCategory(category: String) {
        if (_isLoading.value) {
            Log.d("TipsViewModel", "Already loading, skipping request")
            return
        }
        
        if (category.isBlank()) {
            _error.value = "Please provide a valid category."
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val prompt = "Generate 3 practical and supportive mental health tips for '$category'. " +
                    "Make each tip specific, actionable, and written in a warm, caring tone. " +
                    "Each tip should be under 60 words and feel like advice from a supportive friend. " +
                    "Format as a JSON array of strings."
                
                Log.d("TipsViewModel", "Generating tips for category: $category")
                
                val request = GeminiRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )
                val response = geminiApiService.generateContent(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val aiText = body.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!aiText.isNullOrBlank()) {
                            try {
                                // Try to parse as JSON array
                                val jsonArray = JSONArray(aiText.trim())
                                val newTips = mutableListOf<DailyTip>()
                                
                                for (i in 0 until jsonArray.length()) {
                                    val tipText = jsonArray.getString(i)
                                    if (tipText.isNotBlank()) {
                                        newTips.add(
                                            DailyTip(
                                                id = System.currentTimeMillis().toInt() + i,
                                                text = tipText.trim(),
                                                category = category
                                            )
                                        )
                                    }
                                }
                                
                                if (newTips.isNotEmpty()) {
                                    _aiGeneratedTips.value = _aiGeneratedTips.value + newTips
                                    Log.d("TipsViewModel", "Generated ${newTips.size} tips for category: $category")
                                } else {
                                    _error.value = "No valid tips were generated."
                                }
                            } catch (e: Exception) {
                                Log.e("TipsViewModel", "JSON parsing failed, treating as single tip", e)
                                // If JSON parsing fails, treat as single tip
                                val newTip = DailyTip(
                                    id = System.currentTimeMillis().toInt(),
                                    text = aiText.trim(),
                                    category = category
                                )
                                _aiGeneratedTips.value = _aiGeneratedTips.value + newTip
                            }
                        } else {
                            _error.value = "AI did not return valid tips."
                        }
                    } else {
                        _error.value = "No response from AI. Please try again."
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("TipsViewModel", "API error: ${response.code()} - $errorMsg")
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
                Log.e("TipsViewModel", "Error generating tips by category", e)
                _error.value = when {
                    e.message?.contains("GEMINI_API_KEY") == true -> "API key not configured. Please add your Gemini API key to gradle.properties"
                    e.message?.contains("network", true) == true -> "Network error. Please check your connection."
                    else -> "Failed to generate tips. Please try again."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateMorningTip() {
        generatePersonalizedTip(context = "starting the day with positive energy and setting a good mood for the day ahead")
    }

    fun generateEveningTip() {
        generatePersonalizedTip(context = "winding down, relaxing, and preparing for restful sleep")
    }

    fun generateStressReliefTip() {
        generateTipsByCategory("Stress Relief")
    }

    fun generateAnxietyTip() {
        generateTipsByCategory("Anxiety Management")
    }

    fun generateDepressionTip() {
        generateTipsByCategory("Depression Support")
    }

    fun generateTipFromSuggestion(suggestion: String) {
        generatePersonalizedTip(context = suggestion)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearAIGeneratedTips() {
        _aiGeneratedTips.value = emptyList()
    }

    fun getFavoriteTips(): List<DailyTip> {
        return (_tips.value + _aiGeneratedTips.value).filter { it.isFavorite }
    }

    fun getHelpfulTips(): List<DailyTip> {
        return (_tips.value + _aiGeneratedTips.value).filter { it.isHelpful == true }
    }
}
