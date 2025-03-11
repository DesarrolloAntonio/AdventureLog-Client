package com.desarrollodroide.adventurelog.feature.settings.platform

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Android implementation of BackHandler using androidx.activity.compose.BackHandler
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
