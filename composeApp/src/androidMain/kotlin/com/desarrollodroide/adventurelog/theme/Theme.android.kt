package com.desarrollodroide.adventurelog.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getDynamicColorScheme(isDarkTheme: Boolean): ColorScheme? {
    return if (isDynamicColorSupported) {
        val context = LocalContext.current
        if (isDarkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        null
    }
}