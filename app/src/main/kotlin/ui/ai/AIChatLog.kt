package com.mindeaseai.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import android.util.Log

/**
 * Simple chat log composable for displaying chat history.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatLog(
    messages: List<Pair<String, String>>,
    showGreeting: Boolean = false,
    userName: String? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    Log.d("AIChatLog", "Messages count: ${messages.size}, showGreeting: $showGreeting")
    
    // Scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
            reverseLayout = false,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (showGreeting) {
                item {
                    AnimatedVisibility(visible = true) {
                        ChatBubble(
                            text = "Hello${if (!userName.isNullOrBlank()) ", $userName" else ""}! Welcome to MindEaseAi. How can I help you today?",
                            isUser = false
                        )
                    }
                }
            }
            itemsIndexed(messages) { idx, (user, ai) ->
                Log.d("AIChatLog", "Rendering message $idx: user='$user', ai='$ai'")
                if (user.isNotBlank()) {
                    AnimatedVisibility(visible = true) {
                        ChatBubble(text = user, isUser = true)
                    }
                }
                if (ai.isNotBlank()) {
                    AnimatedVisibility(visible = true) {
                        ChatBubble(text = ai, isUser = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(text: String, isUser: Boolean) {
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val shape: Shape = if (isUser) MaterialTheme.shapes.medium else MaterialTheme.shapes.small
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Filled.SmartToy,
                contentDescription = "AI Avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp).padding(end = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(4.dp)
                .clip(shape)
                .background(bubbleColor)
                .defaultMinSize(minWidth = 48.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                textAlign = if (isUser) TextAlign.End else TextAlign.Start,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isUser) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User Avatar",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp).padding(start = 4.dp)
            )
        }
    }
}
