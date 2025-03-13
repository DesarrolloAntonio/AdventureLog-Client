package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.feature.settings.ui.components.VisualSection
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Provides previews for VisualSection component in Android Studio.
 */
@Preview(
    name = "VisualSection - Light Theme",
    showBackground = true
)
@Composable
fun VisualSectionLightThemePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            VisualSection(
                themeMode = MutableStateFlow(ThemeMode.LIGHT),
                dynamicColors = MutableStateFlow(true)
            )
        }
    }
}

/**
 * Preview of VisualSection with Dark theme mode
 */
@Preview(
    name = "VisualSection - Dark Theme",
    showBackground = true
)
@Composable
fun VisualSectionDarkThemePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            VisualSection(
                themeMode = MutableStateFlow(ThemeMode.DARK),
                dynamicColors = MutableStateFlow(true)
            )
        }
    }
}

/**
 * Preview of VisualSection with Auto theme mode
 */
@Preview(
    name = "VisualSection - Auto Theme",
    showBackground = true
)
@Composable
fun VisualSectionAutoThemePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            VisualSection(
                themeMode = MutableStateFlow(ThemeMode.AUTO),
                dynamicColors = MutableStateFlow(false)
            )
        }
    }
}
