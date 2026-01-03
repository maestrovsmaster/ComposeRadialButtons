package com.radialbuttons.circularbuttonlayout.data

import androidx.compose.ui.graphics.Color

/**
 * Data for single button with icon and text
 */
data class CircularButtonData(
    val icon: String,           // Icon (or emoji)
    val text: String,           // Text
    val onClick: () -> Unit,
    val onIconClick: (() -> Unit)? = null,  // Separate click on icon (if null - onClick is used)
    val iconColor: Color = Color(0xFFFF9800),  // Orange by default
    val textColor: Color = Color.White,
    val radioGroupId: String? = null  // Radio group ID for mutually exclusive buttons (like RADIO/MUSIC)
)
