package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * The web's Advanced section keeps a small block of facts about the account and the build; on a
 * phone they are the first thing anyone asks for when something goes wrong, so they live here
 * next to the links.
 */
@Composable
fun AboutSection(
    user: UserDetails?,
    serverUrl: String,
    appVersion: String,
    onNavigateToServerSettings: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    onSendFeedbackEmail: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        emoji = "🛠️",
        title = "About",
        subtitle = "This account, this server, this build",
        modifier = modifier
    ) {
        if (serverUrl.isNotBlank()) {
            FactRow("Server", serverUrl)
        }
        user?.let {
            FactRow("Signed in as", it.username)
            if (it.uuid.isNotBlank()) FactRow("UUID", it.uuid)
            FactRow("Account", if (it.isStaff) "Staff" else "Basic user")
            FactRow("Profile", if (it.publicProfile) "Public" else "Private")
            if (it.dateJoined.isNotBlank()) {
                FactRow("Joined", it.dateJoined.take(10))
            }
        }
        FactRow("App version", appVersion)

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(4.dp))

        ClickableOption(
            title = "Server Settings Guide",
            icon = Icons.Filled.Dns,
            onClick = onNavigateToServerSettings
        )
        ClickableOption(
            title = "Source Code",
            icon = Icons.Filled.Code,
            onClick = onNavigateToSourceCode
        )
        ClickableOption(
            title = "Send Feedback",
            icon = Icons.Filled.Feedback,
            onClick = onSendFeedbackEmail
        )
        ClickableOption(
            title = "Terms of Use",
            icon = Icons.Filled.Gavel,
            onClick = onNavigateToTermsOfUse
        )
        ClickableOption(
            title = "Privacy policy",
            icon = Icons.Filled.Security,
            onClick = onNavigateToPrivacyPolicy
        )
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.6f)
        )
    }
}
