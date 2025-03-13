package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.settings.ui.components.DebugSection

/**
 * Provides previews for DebugSection component in Android Studio.
 */
@Preview(
    name = "DebugSection - Light Theme",
    showBackground = true
)
@Composable
fun DebugSectionLightThemePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DebugSection(
                onNavigateToLogs = {},
                onViewLastCrash = {}
            )
        }
    }
}

/**
 * Preview of DebugSection with dark theme
 */
@Preview(
    name = "DebugSection - Dark Theme",
    showBackground = true
)
@Composable
fun DebugSectionDarkThemePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DebugSection(
                onNavigateToLogs = {},
                onViewLastCrash = {}
            )
        }
    }
}
