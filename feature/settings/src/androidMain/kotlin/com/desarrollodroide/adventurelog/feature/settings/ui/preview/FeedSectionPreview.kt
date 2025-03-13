package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.settings.ui.components.FeedSection

/**
 * Provides previews for FeedSection component in Android Studio.
 */
@Preview(
    name = "FeedSection - Compact View On",
    showBackground = true
)
@Composable
fun FeedSectionCompactViewOnPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            FeedSection(
                compactView = true,
                onCompactViewChanged = {}
            )
        }
    }
}

/**
 * Preview of FeedSection with compact view off
 */
@Preview(
    name = "FeedSection - Compact View Off",
    showBackground = true
)
@Composable
fun FeedSectionCompactViewOffPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            FeedSection(
                compactView = false,
                onCompactViewChanged = {}
            )
        }
    }
}

/**
 * Preview of FeedSection with dark theme
 */
@Preview(
    name = "FeedSection - Dark Theme",
    showBackground = true
)
@Composable
fun FeedSectionDarkThemePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            FeedSection(
                compactView = true,
                onCompactViewChanged = {}
            )
        }
    }
}
