package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.settings.ui.components.SwitchOption
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.Item
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Provides previews for SwitchOption component in Android Studio.
 */
@Preview(
    name = "SwitchOption - Light On",
    showBackground = true
)
@Composable
fun SwitchOptionLightOnPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwitchOption(
                title = "Dark Mode",
                icon = Icons.Default.DarkMode,
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

/**
 * Preview of SwitchOption in Off state
 */
@Preview(
    name = "SwitchOption - Light Off",
    showBackground = true
)
@Composable
fun SwitchOptionLightOffPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwitchOption(
                title = "Notifications",
                icon = Icons.Default.Notifications,
                checked = false,
                onCheckedChange = {}
            )
        }
    }
}

/**
 * Preview of SwitchOption with dark theme
 */
@Preview(
    name = "SwitchOption - Dark",
    showBackground = true
)
@Composable
fun SwitchOptionDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwitchOption(
                title = "Dark Mode",
                icon = Icons.Default.DarkMode,
                checked = true,
                onCheckedChange = {}
            )
        }
    }
}

/**
 * Preview of SwitchOption using Item parameter
 */
@Preview(
    name = "SwitchOption - Using Item",
    showBackground = true
)
@Composable
fun SwitchOptionWithItemPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val switchState = MutableStateFlow(true)
            SwitchOption(
                item = Item(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    switchState = switchState
                ),
                switchState = switchState
            )
        }
    }
}
