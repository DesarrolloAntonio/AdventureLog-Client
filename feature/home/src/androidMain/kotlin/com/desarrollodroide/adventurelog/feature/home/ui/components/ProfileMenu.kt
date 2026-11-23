package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.ui.components.settings.AccountHeader
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.settings.LocalSettingsIconContainer
import com.desarrollodroide.adventurelog.feature.ui.components.settings.LocalSettingsSegmentColor
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import androidx.compose.material.icons.outlined.Group

/**
 * Everything that used to sit at the top of the drawer: who you are, settings, and the way out.
 *
 * It opens as a sheet rather than a dropdown pinned under the avatar. A dropdown puts the two
 * things you came for - and one of them is Log out - in the far top corner, which is the one part
 * of a phone a thumb cannot reach; and it was a grey rectangle in an app that has no other grey
 * rectangles in it.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileMenu(
    user: UserDetails?,
    userName: String,
    serverUrl: String,
    onSettings: () -> Unit,
    onCalendar: () -> Unit,
    onUsers: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }

    IconButton(onClick = { open = true }, shapes = IconButtonDefaults.shapes(), modifier = modifier) {
        Avatar(user = user, userName = userName, serverUrl = serverUrl)
    }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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

                // The rows as the settings screen draws them: one segmented block, each icon on a
                // tinted square.
                MenuBlock {
                    // The calendar is not a place you switch to constantly, so it does not earn one
                    // of the five slots in the bottom bar - but it does have to be reachable from
                    // more than the one link on Home.
                    SettingsRow(
                        title = "Calendar",
                        icon = Icons.Outlined.CalendarMonth,
                        supporting = "Everything with a date on it",
                        onClick = {
                            open = false
                            onCalendar()
                        }
                    )
                    // The web has this as a top-level section; here it sits beside the calendar,
                    // since browsing other travellers is something you do occasionally, not daily.
                    SettingsRow(
                        title = "People",
                        icon = Icons.Outlined.Group,
                        supporting = "Travellers with a public profile",
                        onClick = {
                            open = false
                            onUsers()
                        }
                    )
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
                        SettingsRow(
                            title = "Server",
                            icon = Icons.Outlined.Dns,
                            supporting = serverUrl,
                            showChevron = false
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Last, and a block of its own: the only thing here you cannot undo with another tap.
                SignOutRow(
                    onClick = {
                        open = false
                        onLogout()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/** Settings rows cut as one segmented block on the sheet: the gap between them is the divider. */
@Composable
private fun MenuBlock(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(SegmentedListDefaults.single),
        verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)
    ) {
        CompositionLocalProvider(
            LocalSettingsSegmentColor provides MaterialTheme.colorScheme.surfaceContainerHigh,
            LocalSettingsIconContainer provides MaterialTheme.colorScheme.secondaryContainer,
        ) { content() }
    }
}

/**
 * Sign out, red in its words and its square. Not a [SettingsRow]: a tinted one drops the square,
 * and its title would start further left than every row above it.
 */
@Composable
private fun SignOutRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SegmentedListDefaults.single)
            .background(colors.surfaceContainerHigh)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = colors.onErrorContainer,
                modifier = Modifier.size(22.dp)
            )
        }
        Text("Sign out", style = MaterialTheme.typography.bodyLarge, color = colors.error)
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
