package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRowDivider

/**
 * Who a collection is shared with, and who else it could be.
 *
 * The three endpoints behind this - share, unshare, revoke-invite - have existed the whole time
 * with nothing calling them: the app could answer an invitation but never send one.
 *
 * Only a public profile can be invited; the server refuses everyone else. So the list of people
 * is the list of people with public profiles, and when it is empty the sheet says why rather than
 * leaving someone to wonder what they did wrong.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCollectionSheet(
    collectionName: String,
    state: ShareSheetState,
    onInvite: (PublicUser) -> Unit,
    onRevoke: (PublicUser) -> Unit,
    onUnshare: (PublicUser) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Share",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp)
            )
            Text(
                text = collectionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            )

            when {
                state.isLoading -> Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                }

                state.error != null -> Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = onRetry) { Text("Try again") }
                }

                state.people.isEmpty() -> Text(
                    text = "Nobody else on this server has a public profile, so there is no one " +
                        "to share with yet. A profile becomes public in that person's own " +
                        "settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                else -> {
                    val shared = state.people.filter { it.uuid in state.sharedWith }
                    val invited = state.people.filter {
                        it.uuid in state.invitedThisVisit && it.uuid !in state.sharedWith
                    }
                    val rest = state.people - shared.toSet() - invited.toSet()

                    if (shared.isNotEmpty()) {
                        SectionLabel("Shared with")
                        shared.forEachIndexed { index, person ->
                            if (index > 0) SettingsRowDivider()
                            PersonRow(
                                person = person,
                                actionLabel = "Remove",
                                busy = state.busyUuid == person.uuid,
                                onAction = { onUnshare(person) }
                            )
                        }
                    }

                    if (invited.isNotEmpty()) {
                        // Only the invitations sent in this sitting: the server offers no way to
                        // list what is already pending, so claiming to know would be a guess.
                        SectionLabel("Invited just now")
                        invited.forEachIndexed { index, person ->
                            if (index > 0) SettingsRowDivider()
                            PersonRow(
                                person = person,
                                actionLabel = "Revoke",
                                busy = state.busyUuid == person.uuid,
                                onAction = { onRevoke(person) }
                            )
                        }
                    }

                    if (rest.isNotEmpty()) {
                        SectionLabel(if (shared.isEmpty() && invited.isEmpty()) "People" else "Invite someone else")
                        rest.forEachIndexed { index, person ->
                            if (index > 0) SettingsRowDivider()
                            PersonRow(
                                person = person,
                                actionLabel = "Invite",
                                busy = state.busyUuid == person.uuid,
                                onAction = { onInvite(person) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** What the sheet knows. [invitedThisVisit] is deliberately not persisted - see the sheet's note. */
data class ShareSheetState(
    val people: List<PublicUser> = emptyList(),
    val sharedWith: Set<String> = emptySet(),
    val invitedThisVisit: Set<String> = emptySet(),
    val busyUuid: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@Composable
private fun SectionLabel(text: String) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, bottom = 4.dp)
    )
}

@Composable
private fun PersonRow(
    person: PublicUser,
    actionLabel: String,
    busy: Boolean,
    onAction: () -> Unit
) {
    SettingsRow(
        title = person.displayName,
        supporting = "@${person.username}",
        icon = if (actionLabel == "Invite") Icons.Outlined.PersonAdd else Icons.Outlined.PersonRemove,
        showChevron = false,
        trailing = {
            if (busy) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    )
}
