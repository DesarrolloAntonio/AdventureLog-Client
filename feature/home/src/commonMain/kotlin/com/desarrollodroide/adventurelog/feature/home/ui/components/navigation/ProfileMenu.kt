package com.desarrollodroide.adventurelog.feature.home.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.ui.components.settings.AccountHeader
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRowDivider

/**
 * Everything that used to sit at the top of the drawer: who you are, settings, and the way out.
 *
 * It opens as a sheet rather than a dropdown pinned under the avatar. A dropdown puts the two
 * things you came for - and one of them is Log out - in the far top corner, which is the one part
 * of a phone a thumb cannot reach; and it was a grey rectangle in an app that has no other grey
 * rectangles in it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenu(
    user: UserDetails?,
    userName: String,
    serverUrl: String,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }

    IconButton(onClick = { open = true }, modifier = modifier) {
        Avatar(user = user, userName = userName, serverUrl = serverUrl)
    }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                AccountHeader(
                    user = user,
                    primaryEmail = user?.email,
                    serverUrl = serverUrl,
                    actionIcon = Icons.Outlined.Settings,
                    actionDescription = "Open settings",
                    onClick = {
                        open = false
                        onSettings()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(20.dp))

                SettingsRow(
                    title = "Settings",
                    icon = Icons.Outlined.Settings,
                    supporting = "Your profile, sign-in and how the app looks",
                    onClick = {
                        open = false
                        onSettings()
                    }
                )
                if (serverUrl.isNotBlank()) {
                    SettingsRowDivider()
                    SettingsRow(
                        title = "Server",
                        icon = Icons.Outlined.Dns,
                        supporting = serverUrl,
                        showChevron = false
                    )
                }
                // Last, and on its own: the only thing here you cannot undo with another tap.
                SettingsRowDivider()
                SettingsRow(
                    title = "Sign out",
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    tint = MaterialTheme.colorScheme.error,
                    showChevron = false,
                    onClick = {
                        open = false
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
private fun Avatar(user: UserDetails?, userName: String, serverUrl: String) {
    val picture = resolvePicture(user?.profilePic, serverUrl)

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (picture != null) {
            Image(
                painter = rememberAsyncImagePainter(model = picture),
                contentDescription = "Your account",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(34.dp).clip(CircleShape)
            )
        } else {
            Text(
                text = userName.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

private fun resolvePicture(raw: String?, serverUrl: String): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    if (serverUrl.isBlank()) return null
    return serverUrl.trimEnd('/') + "/" + value.trimStart('/')
}
