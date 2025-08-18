package com.mindeaseai.ui.tips

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.mindeaseai.tips.TipsViewModel

@Composable
fun DailyTipsFlow() {
    val tipsViewModel: TipsViewModel = hiltViewModel()
    val tips by tipsViewModel.tips.collectAsState()
    val aiGeneratedTips by tipsViewModel.aiGeneratedTips.collectAsState()
    val suggestions by tipsViewModel.suggestions.collectAsState()
    val isLoading by tipsViewModel.isLoading.collectAsState()
    val errorMessage by tipsViewModel.error.collectAsState()

    // Ensure tips are loaded when the screen is first displayed
    LaunchedEffect(Unit) {
        if (tips.isEmpty()) {
            tipsViewModel.loadTips()
        }
    }

    DailyTipsScreen(
        tips = tips,
        aiGeneratedTips = aiGeneratedTips,
        suggestions = suggestions,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onGeneratePersonalizedTip = { tipsViewModel.generatePersonalizedTip() },
        onGenerateMorningTip = { tipsViewModel.generateMorningTip() },
        onGenerateEveningTip = { tipsViewModel.generateEveningTip() },
        onGenerateStressReliefTip = { tipsViewModel.generateStressReliefTip() },
        onGenerateAnxietyTip = { tipsViewModel.generateAnxietyTip() },
        onGenerateDepressionTip = { tipsViewModel.generateDepressionTip() },
        onGenerateFromSuggestion = { suggestion -> tipsViewModel.generateTipFromSuggestion(suggestion) },
        onClearAITips = { tipsViewModel.clearAIGeneratedTips() }
    )
}
