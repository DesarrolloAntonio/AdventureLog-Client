package com.desarrollodroide.adventurelog.feature.settings.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.adventurelog.feature.settings.ui.components.AccountSection

/**
 * Provides a preview for AccountSection in Android Studio.
 */
@Preview(
    name = "Account Section Preview - Light",
    showBackground = true
)
@Composable
fun AccountSectionLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AccountSection(
                serverUrl = "https://example-server.com",
                onLogout = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                onNavigateToSeverSettings = {},
                onSendFeedbackEmail = {},
                onNavigateToSourceCode = {}
            )
        }
    }
}

@Preview(
    name = "Account Section Preview - Dark",
    showBackground = true
)
@Composable
fun AccountSectionDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AccountSection(
                serverUrl = "https://server-dark-mode.com",
                onLogout = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                onNavigateToSeverSettings = {},
                onSendFeedbackEmail = {},
                onNavigateToSourceCode = {}
            )
        }
    }
}

@Preview(
    name = "Account Section Preview - Empty Server",
    showBackground = true
)
@Composable
fun AccountSectionNoServerPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AccountSection(
                serverUrl = "",
                onLogout = {},
                onNavigateToTermsOfUse = {},
                onNavigateToPrivacyPolicy = {},
                onNavigateToSeverSettings = {},
                onSendFeedbackEmail = {},
                onNavigateToSourceCode = {}
            )
        }
    }
}