package com.mindeaseai.ui.tips

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.mindeaseai.ui.tips.DailyTip
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.platform.LocalContext
import com.mindeaseai.ui.dailyTipsEnabledFlow
import com.mindeaseai.ui.setDailyTipsEnabled
import kotlinx.coroutines.flow.first
import com.mindeaseai.notifications.DailyTipScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTipsScreen(
    tips: List<DailyTip>,
    aiGeneratedTips: List<DailyTip> = emptyList(),
    suggestions: List<String> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onGeneratePersonalizedTip: () -> Unit = {},
    onGenerateMorningTip: () -> Unit = {},
    onGenerateEveningTip: () -> Unit = {},
    onGenerateStressReliefTip: () -> Unit = {},
    onGenerateAnxietyTip: () -> Unit = {},
    onGenerateDepressionTip: () -> Unit = {},
    onGenerateFromSuggestion: (String) -> Unit = {},
    onClearAITips: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }
    
    // State for category filter, favorites, and tip of the day
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var tipsState by remember { mutableStateOf(tips) }
    var aiTipsState by remember { mutableStateOf(aiGeneratedTips) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(true) }
    
    // Update tips when they change
    LaunchedEffect(tips) {
        tipsState = tips
    }
    
    // Update AI tips when they change
    LaunchedEffect(aiGeneratedTips) {
        aiTipsState = aiGeneratedTips
    }
    
    val allCategories = (tips + aiGeneratedTips).distinctBy { it.category }.map { it.category }
    
    // Safe tip of the day calculation
    val todayTip = if (tips.isNotEmpty()) {
        val dayIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt() % tips.size
        tips.getOrNull(dayIndex) ?: tips.firstOrNull()
    } else {
        null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Daily Tips", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Your mental wellness companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (aiGeneratedTips.isNotEmpty()) {
                        IconButton(onClick = onClearAITips) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear AI Tips")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val context = LocalContext.current
        var tipsEnabled by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            tipsEnabled = dailyTipsEnabledFlow(context).first()
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tip of the Day
            todayTip?.let { tip ->
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Lightbulb,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Tip of the Day",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Daily Tips", style = MaterialTheme.typography.labelMedium)
                                        Switch(
                                            checked = tipsEnabled,
                                            onCheckedChange = { enabled ->
                                                tipsEnabled = enabled
                                                coroutineScope.launch {
                                                    setDailyTipsEnabled(context, enabled)
                                                    if (enabled) DailyTipScheduler.enable(context) else DailyTipScheduler.disable(context)
                                                    snackbarHostState.showSnackbar(
                                                        if (enabled) "Daily tips enabled" else "Daily tips disabled"
                                                    )
                                                }
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    tip.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Suggestions Section
            if (suggestions.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showSuggestions,
                        enter = fadeIn() + expandVertically(animationSpec = tween(400))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Quick Suggestions",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { showSuggestions = !showSuggestions },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Clear,
                                            contentDescription = "Hide suggestions",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Tap any suggestion to get a personalized tip:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(suggestions.take(6)) { suggestion ->
                                        FilterChip(
                                            selected = false,
                                            onClick = { onGenerateFromSuggestion(suggestion) },
                                            enabled = !isLoading,
                                            label = { 
                                                Text(
                                                    suggestion,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // AI Generation Section
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(animationSpec = tween(600))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "AI-Powered Tips",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            // Quick generation buttons
                            Text(
                                "Quick Tips",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onGeneratePersonalizedTip,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Personalized", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = onGenerateMorningTip,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Morning", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = onGenerateEveningTip,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Evening", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            
                            // Category-specific buttons
                            Text(
                                "Mental Health Focus",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onGenerateStressReliefTip,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Stress", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = onGenerateAnxietyTip,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Anxiety", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = onGenerateDepressionTip,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Depression", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            
                            if (isLoading) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Generating...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Filter Section
            if (allCategories.isNotEmpty() || (tipsState + aiTipsState).any { it.isFavorite }) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically(animationSpec = tween(800))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Filter Tips",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                // Favorites filter
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = showFavoritesOnly,
                                        onClick = { showFavoritesOnly = !showFavoritesOnly },
                                        label = { Text("Favorites") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Favorite,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                    
                                    FilterChip(
                                        selected = selectedCategory == null && !showFavoritesOnly,
                                        onClick = { 
                                            selectedCategory = null
                                            showFavoritesOnly = false
                                        },
                                        label = { Text("All") }
                                    )
                                }
                                
                                // Category filters
                                if (allCategories.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(allCategories) { cat ->
                                            FilterChip(
                                                selected = selectedCategory == cat && !showFavoritesOnly,
                                                onClick = { 
                                                    selectedCategory = cat
                                                    showFavoritesOnly = false
                                                },
                                                label = { Text(cat) },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Filled.Category,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Tips list
            val allTips = tipsState + aiTipsState
            val filteredTips = allTips.filter { tip ->
                val categoryMatch = selectedCategory == null || tip.category == selectedCategory
                val favoriteMatch = !showFavoritesOnly || tip.isFavorite
                categoryMatch && favoriteMatch
            }
            
            if (filteredTips.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    if (showFavoritesOnly) "No favorite tips yet" else "No tips available",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (showFavoritesOnly) 
                                        "Start favoriting tips you find helpful!" 
                                    else 
                                        "Try generating some AI-powered tips above!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredTips,
                    key = { it.id }
                ) { tip ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        TipCard(
                            tip = tip,
                            onFavoriteToggle = {
                                if (tip.category == "AI Generated") {
                                    aiTipsState = aiTipsState.map {
                                        if (it.id == tip.id) it.copy(isFavorite = !it.isFavorite) else it
                                    }
                                } else {
                                    tipsState = tipsState.map {
                                        if (it.id == tip.id) it.copy(isFavorite = !it.isFavorite) else it
                                    }
                                }
                            },
                            onHelpfulToggle = { isHelpful ->
                                if (tip.category == "AI Generated") {
                                    aiTipsState = aiTipsState.map {
                                        if (it.id == tip.id) it.copy(isHelpful = isHelpful) else it
                                    }
                                } else {
                                    tipsState = tipsState.map {
                                        if (it.id == tip.id) it.copy(isHelpful = isHelpful) else it
                                    }
                                }
                            },
                            onShare = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Tip copied to clipboard!")
                                }
                            }
                        )
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TipCard(
    tip: DailyTip,
    onFavoriteToggle: () -> Unit,
    onHelpfulToggle: (Boolean) -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tip.category == "AI Generated") 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        tip.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    if (tip.category == "AI Generated") {
                        Icon(
                            Icons.Filled.Psychology,
                            contentDescription = "AI Generated",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (tip.isFavorite) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Favorited",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Tip text
            Text(
                tip.text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (tip.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (tip.isFavorite) "Unfavorite" else "Favorite",
                        tint = if (tip.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Share button
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Helpful/Not Helpful buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onHelpfulToggle(true) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = "Helpful",
                            tint = if (tip.isHelpful == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onHelpfulToggle(false) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbDown,
                            contentDescription = "Not Helpful",
                            tint = if (tip.isHelpful == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
