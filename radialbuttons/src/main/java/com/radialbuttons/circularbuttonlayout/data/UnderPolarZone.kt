package com.radialbuttons.circularbuttonlayout.data

import androidx.compose.runtime.Composable

/**
 * Under-polar zone with custom Compose content
 * Can contain list of Composable children arranged horizontally
 */
data class UnderPolarZone(
    val children: List<@Composable () -> Unit> = emptyList()
)
