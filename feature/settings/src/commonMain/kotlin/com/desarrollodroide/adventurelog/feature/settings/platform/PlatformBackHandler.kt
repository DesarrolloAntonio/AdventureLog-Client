package com.desarrollodroide.adventurelog.feature.settings.platform

import androidx.compose.runtime.Composable

/**
 * Expect declaration for a cross-platform BackHandler
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
