package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.settings.ui.components.ClickableOption
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.Item

/**
 * Provides previews for ClickableOption component in Android Studio.
 */
@Preview(
    name = "ClickableOption - Light",
    showBackground = true
)
@Composable
fun ClickableOptionLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ClickableOption(
                title = "Clear Cache",
                icon = Icons.Default.Delete,
                subtitle = "Current size: 24 MB",
                onClick = {}
            )
        }
    }
}

/**
 * Preview of ClickableOption with dark theme
 */
@Preview(
    name = "ClickableOption - Dark",
    showBackground = true
)
@Composable
fun ClickableOptionDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ClickableOption(
                title = "Clear Cache",
                icon = Icons.Default.Delete,
                subtitle = "Current size: 24 MB",
                onClick = {}
            )
        }
    }
}

/**
 * Preview of ClickableOption without subtitle
 */
@Preview(
    name = "ClickableOption - No Subtitle",
    showBackground = true
)
@Composable
fun ClickableOptionNoSubtitlePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ClickableOption(
                title = "About",
                icon = Icons.Default.Info,
                onClick = {}
            )
        }
    }
}

/**
 * Preview of ClickableOption using Item parameter
 */
@Preview(
    name = "ClickableOption - Using Item",
    showBackground = true
)
@Composable
fun ClickableOptionWithItemPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ClickableOption(
                item = Item(
                    title = "About Application",
                    icon = Icons.Default.Info,
                    subtitle = "Version 1.0.0",
                    onClick = {}
                )
            )
        }
    }
}
