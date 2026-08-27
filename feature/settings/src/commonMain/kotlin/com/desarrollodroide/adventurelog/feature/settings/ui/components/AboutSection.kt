package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRowDivider
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsGroup

/**
 * Where this account lives and what build is talking to it - the first questions anyone asks when
 * something goes wrong - followed by the links.
 */
@Composable
fun AboutSection(
    user: UserDetails?,
    serverUrl: String,
    appVersion: String,
    onNavigateToServerGuide: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    onSendFeedbackEmail: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsGroup(title = "About") {
            SettingsRow(
                title = "Server",
                icon = Icons.Outlined.Dns,
                supporting = serverUrl.ifBlank { "Not connected" },
                showChevron = false
            )
            user?.let {
                SettingsRowDivider()
                SettingsRow(
                    title = "Account",
                    icon = Icons.Outlined.Badge,
                    supporting = buildString {
                        append(if (it.isStaff) "Staff" else "Basic user")
                        val joined = it.dateJoined.take(10)
                        if (joined.isNotBlank()) append(" · joined $joined")
                    },
                    showChevron = false
                )
            }
            SettingsRowDivider()
            SettingsRow(
                title = "App version",
                icon = Icons.Outlined.Info,
                supporting = appVersion.ifBlank { "Unknown" },
                showChevron = false
            )
            SettingsRowDivider(inset = false)
            SettingsRow(
                title = "Server setup guide",
                icon = Icons.Outlined.MenuBook,
                onClick = onNavigateToServerGuide
            )
            SettingsRowDivider()
            SettingsRow(
                title = "Source code",
                icon = Icons.Outlined.Code,
                onClick = onNavigateToSourceCode
            )
            SettingsRowDivider()
            SettingsRow(
                title = "Send feedback",
                icon = Icons.Outlined.Feedback,
                onClick = onSendFeedbackEmail
            )
            SettingsRowDivider()
            SettingsRow(
                title = "Terms of use",
                icon = Icons.Outlined.Gavel,
                onClick = onNavigateToTermsOfUse
            )
            SettingsRowDivider()
            SettingsRow(
                title = "Privacy policy",
                icon = Icons.Outlined.Security,
                onClick = onNavigateToPrivacyPolicy
            )
        }

        // The account's UUID matters exactly once - when someone administering the server asks
        // for it - so it sits in the footer rather than taking a row of its own.
        user?.uuid?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(20.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
        }
    }
}
