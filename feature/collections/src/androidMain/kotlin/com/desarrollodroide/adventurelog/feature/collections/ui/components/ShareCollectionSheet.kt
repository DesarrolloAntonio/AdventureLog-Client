package com.desarrollodroide.adventurelog.feature.collections.ui.components

import com.desarrollodroide.adventurelog.feature.ui.components.CookieBadge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = "Share", style = AdventureType.display(34.sp))
                Text(
                    text = collectionName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // A modal sheet is a window of its own: a snackbar raised by the screen underneath is
            // drawn behind it, so the sheet says what happened itself.
            state.message?.let { message ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (message.isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }

                state.error != null -> Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = onRetry, shapes = ButtonDefaults.shapes()) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Try again", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                state.people.isEmpty() -> Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CookieBadge(icon = Icons.Outlined.Group, container = MaterialTheme.colorScheme.secondaryContainer, content = MaterialTheme.colorScheme.onSecondaryContainer, size = 72.dp)
                    Text(
                        text = "Nobody else on this server has a public profile, so there is no one " +
                            "to share with yet. A profile becomes public in that person's own " +
                            "settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    val shared = state.people.filter { it.uuid in state.sharedWith }
                    val invited = state.people.filter {
                        it.uuid in state.invitedThisVisit && it.uuid !in state.sharedWith
                    }
                    val rest = state.people - shared.toSet() - invited.toSet()

                    if (shared.isNotEmpty()) {
                        PeopleGroup("Shared with", shared, "Remove", state.busyUuid, onUnshare)
                    }

                    if (invited.isNotEmpty()) {
                        // Only the invitations sent in this sitting: the server offers no way to
                        // list what is already pending, so claiming to know would be a guess.
                        PeopleGroup("Invited just now", invited, "Revoke", state.busyUuid, onRevoke)
                    }

                    if (rest.isNotEmpty()) {
                        PeopleGroup(
                            title = if (shared.isEmpty() && invited.isEmpty()) "People" else "Invite someone else",
                            people = rest,
                            actionLabel = "Invite",
                            busyUuid = state.busyUuid,
                            onAction = onInvite
                        )
                    }
                }
            }
        }
    }
}

/** A heading in the label voice, then its people as one segmented block. */
@Composable
private fun PeopleGroup(
    title: String,
    people: List<PublicUser>,
    actionLabel: String,
    busyUuid: String?,
    onAction: (PublicUser) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
            people.forEachIndexed { index, person ->
                PersonRow(
                    person = person,
                    shape = SegmentedListDefaults.shapeFor(index, people.size),
                    actionLabel = actionLabel,
                    busy = busyUuid == person.uuid,
                    onAction = { onAction(person) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PersonRow(
    person: PublicUser,
    shape: Shape,
    actionLabel: String,
    busy: Boolean,
    onAction: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = shape, color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // A person is a round avatar, as on the People screen; squares are for things.
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = person.displayName.trim().firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${person.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (busy) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    LoadingIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (actionLabel == "Invite") {
                Button(onClick = onAction, shapes = ButtonDefaults.shapes()) { Text(actionLabel) }
            } else {
                // Remove and Revoke undo something: tonal, not the loud fill an invitation gets.
                FilledTonalButton(onClick = onAction, shapes = ButtonDefaults.shapes()) { Text(actionLabel) }
            }
        }
    }
}
