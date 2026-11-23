package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The actions available on a location.
 *
 * A sheet rather than a dropdown anchored to the overflow button: the dropdown opened above or
 * below depending on where the card sat, so the same action landed in a different place on every
 * card, and its rows were smaller than a comfortable touch target.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocationActionsSheet(
    locationName: String,
    locationPlace: String?,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onManageCollections: () -> Unit,
    onDelete: () -> Unit,
    /**
     * False for someone else's place inside a collection shared with you. The server lets you edit
     * or duplicate it, but refuses a delete (403) and taking it out of the owner's collections (400),
     * so those two are not offered (measured with two accounts, QA 09).
     */
    isOwner: Boolean = true,
    /** Offered inside a collection, where Delete would take the place out of every other one too. */
    onRemoveFromCollection: (() -> Unit)? = null,
    /** "Add photo", for a place without one: the editor opened on its images. */
    onAddPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // Fully open: half open cut Delete off under the gesture bar once the sheet grew a row.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHeader(name = locationName, place = locationPlace)

            // Three blocks: looking at and changing the place, where it is filed, and the one that
            // cannot be taken back on its own.
            ActionGroup(
                buildList {
                    add(PlaceAction(Icons.Outlined.Info, "Open details", onOpenDetails))
                    add(PlaceAction(Icons.Outlined.Edit, "Edit place", onEdit))
                    onAddPhoto?.let { add(PlaceAction(Icons.Outlined.AddAPhoto, "Add photo", it)) }
                    add(PlaceAction(Icons.Outlined.ContentCopy, "Duplicate", onDuplicate))
                    add(PlaceAction(Icons.Outlined.Share, "Share externally", onShare))
                }
            )
            if (isOwner) {
                ActionGroup(
                    buildList {
                        add(PlaceAction(Icons.Outlined.FolderOpen, "Manage collections", onManageCollections))
                        onRemoveFromCollection?.let { add(PlaceAction(Icons.Outlined.FolderOff, "Remove from collection", it)) }
                    }
                )
                ActionGroup(listOf(PlaceAction(Icons.Outlined.Delete, "Delete", onDelete, destructive = true)))
            }
        }
    }
}

private class PlaceAction(val icon: ImageVector, val label: String, val onClick: () -> Unit, val destructive: Boolean = false)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetHeader(name: String, place: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "?",
                style = AdventureType.display(26.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = AdventureType.display(30.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!place.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = place,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Actions as one segmented block: rounded outside, split by the ground showing through. */
@Composable
private fun ActionGroup(actions: List<PlaceAction>) {
    Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
        actions.forEachIndexed { index, action ->
            ActionRow(action, SegmentedListDefaults.shapeFor(index, actions.size))
        }
    }
}

@Composable
private fun ActionRow(action: PlaceAction, shape: androidx.compose.ui.graphics.Shape) {
    val colors = MaterialTheme.colorScheme
    val badge: Color = if (action.destructive) colors.errorContainer else colors.surfaceContainerHighest
    val badgeContent: Color = if (action.destructive) colors.onErrorContainer else colors.onSurfaceVariant
    Surface(shape = shape, color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(role = Role.Button, onClick = action.onClick)
                .heightIn(min = 60.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = badge, contentColor = badgeContent, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(action.icon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = action.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (action.destructive) colors.error else colors.onSurface
            )
        }
    }
}
