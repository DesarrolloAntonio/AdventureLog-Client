package com.desarrollodroide.adventurelog.feature.ui.platform

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android implementation of BackHandler using androidx.activity.compose.BackHandler
 */
@Composable
fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
