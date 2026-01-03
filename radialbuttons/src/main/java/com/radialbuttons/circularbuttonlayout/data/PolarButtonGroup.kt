package com.radialbuttons.circularbuttonlayout.data

import androidx.compose.ui.graphics.Color

/**
 * Polar button group (top or bottom zone)
 * Visually appears as single area, but contains two separate buttons
 */
data class PolarButtonGroup(
    val leftButton: CircularButtonData,   // Left half
    val rightButton: CircularButtonData,  // Right half
    val title: String? = null,            // Title in center
    val subtitle: String? = null,         // Subtitle in center
    val titleSize: Float = 32f,           // Title text size
    val titleColor: Color = Color.White,  // Title text color
    val subtitleSize: Float = 24f,        // Subtitle text size
    val subtitleColor: Color = Color.White.copy(alpha = 0.7f), // Subtitle text color
    val iconOffsetFromEdge: Float = 0.12f // Icon offset from component edge (0.0 - 0.5)
)
