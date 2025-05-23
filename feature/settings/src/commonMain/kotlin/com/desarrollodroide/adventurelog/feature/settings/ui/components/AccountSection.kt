package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.Item

@Composable
fun AccountSection(
    serverUrl: String,
    onLogout: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSeverSettings: () -> Unit,
    onSendFeedbackEmail: () -> Unit,
    onNavigateToSourceCode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 5.dp)
    ) {
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        ClickableOption(
            Item(
                title = "Logout",
                subtitle = serverUrl,
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = onLogout
            ),
        )
        ClickableOption(
            Item(
                title = "Server Settings Guide",
                icon = Icons.Filled.Dns,
                onClick = onNavigateToSeverSettings
            )
        )
        ClickableOption(
            item = Item(
                title = "Source Code",
                icon = Icons.Filled.Code,
                onClick = onNavigateToSourceCode
            )
        )
        ClickableOption(
            Item(
                title = "Send Feedback",
                icon = Icons.Filled.Feedback,
                onClick = onSendFeedbackEmail
            )
        )
        ClickableOption(
            Item(
                title = "Terms of Use",
                icon = Icons.Filled.Gavel,
                onClick = onNavigateToTermsOfUse
            )
        )
        ClickableOption(
            Item(
                title = "Privacy policy",
                icon = Icons.Filled.Security,
                onClick = onNavigateToPrivacyPolicy
            )
        )
    }
}
