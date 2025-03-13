package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsContent
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Provides previews for the Settings Screen in Android Studio.
 */
@Preview(
    name = "Settings Screen - Light Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun SettingsScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            SettingsContent(
                compactView = false,
                onCompactViewChanged = {},
                onLogout = {},
                onNavigateToSourceCode = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                onNavigateToLogs = {},
                onViewLastCrash = {},
                themeMode = remember { MutableStateFlow(ThemeMode.LIGHT) },
                onThemeModeChanged = {},
                useDynamicColors = remember { MutableStateFlow(true) },
                onDynamicColorsChanged = {},
                goToLogin = {},
                serverUrl = "https://example-server.com"
            )
        }
    }
}

/**
 * Preview of Settings Screen with dark theme
 */
@Preview(
    name = "Settings Screen - Dark Theme",
    showBackground = true,
    heightDp = 800
)
@Composable
fun SettingsScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            SettingsContent(
                compactView = true,
                onCompactViewChanged = {},
                onLogout = {},
                onNavigateToSourceCode = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                onNavigateToLogs = {},
                onViewLastCrash = {},
                themeMode = remember { MutableStateFlow(ThemeMode.DARK) },
                onThemeModeChanged = {},
                useDynamicColors = remember { MutableStateFlow(false) },
                onDynamicColorsChanged = {},
                goToLogin = {},
                serverUrl = "https://example-server.com"
            )
        }
    }
}
