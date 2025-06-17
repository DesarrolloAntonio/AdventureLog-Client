package com.desarrollodroide.adventurelog.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun getDynamicColorScheme(isDarkTheme: Boolean): ColorScheme? {
    // iOS doesn't support dynamic color schemes
    return null
}