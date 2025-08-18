package com.mindeaseai.ui.tips

data class DailyTip(
    val id: Int,
    val text: String,
    val category: String,
    var isFavorite: Boolean = false,
    var isHelpful: Boolean? = null // null = not rated, true = helpful, false = not helpful
)
