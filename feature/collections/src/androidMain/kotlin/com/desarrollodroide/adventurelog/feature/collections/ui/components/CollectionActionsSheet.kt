package com.desarrollodroide.adventurelog.feature.collections.ui.components

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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType

/**
 * The actions available on a collection, matching what the web offers.
 *
 * A sheet for the same reason the location one is: a dropdown anchored to the overflow button
 * moves depending on where the card sits, and eight entries do not fit comfortably in one.
 * The actions come in segmented blocks - getting to it, copying it out, and Delete on its own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionActionsSheet(
    collection: UltraSlimCollection,
    busyLabel: String?,
    /**
     * False for a collection someone shared with you. The server answers its owner-only actions -
     * edit, share, duplicate, export ZIP, archive, delete - with a 404, so they are not offered.
     * Open, the share card and the PDF are allowed and stay (measured with two accounts, QA 09).
     */
    isOwner: Boolean = true,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onShareWithPeople: () -> Unit,
    onArchive: () -> Unit,
    onDownloadPdf: () -> Unit,
    onExportZip: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reach = buildList {
        add(SheetAction(Icons.Outlined.Info, "Open collection", onOpen))
        if (isOwner) {
            add(SheetAction(Icons.Outlined.Edit, "Edit collection", onEdit))
            add(SheetAction(Icons.Outlined.PersonAdd, "Share with people", onShareWithPeople))
        }
        add(SheetAction(Icons.Outlined.Share, "Share externally", onShare))
    }
    val copies = buildList {
        if (isOwner) add(SheetAction(Icons.Outlined.ContentCopy, "Duplicate", onDuplicate))
        add(SheetAction(Icons.Outlined.PictureAsPdf, "Download PDF", onDownloadPdf))
        if (isOwner) {
            add(SheetAction(Icons.Outlined.FolderZip, "Export ZIP", onExportZip))
            add(
                SheetAction(
                    icon = if (collection.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                    label = if (collection.isArchived) "Restore from archive" else "Archive",
                    onClick = onArchive
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHeader(collection)

            if (busyLabel != null) {
                BusyRow(busyLabel)
            }

            if (!isOwner) {
                Text(
                    text = "Shared with you. Only its owner can change, share or delete it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            ActionGroup(reach)
            ActionGroup(copies)
            if (isOwner) {
                // Alone, at the end: the one action that cannot be taken back.
                ActionGroup(listOf(SheetAction(Icons.Outlined.Delete, "Delete", onDelete, destructive = true)))
            }
        }
    }
}

private class SheetAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val destructive: Boolean = false
)

/** The collection's initial on a cookie, its name in the display voice and how many places it holds. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SheetHeader(collection: UltraSlimCollection) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = collection.name.trim().firstOrNull()?.uppercase() ?: "?",
                style = AdventureType.display(24.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = collection.name,
                style = AdventureType.display(30.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${collection.adventureCount} " +
                    if (collection.adventureCount == 1) "place" else "places",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Exports take a moment to render server-side, so the sheet says what it is waiting for. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BusyRow(label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LoadingIndicator(modifier = Modifier.size(32.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActionGroup(actions: List<SheetAction>) {
    Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
        actions.forEachIndexed { index, action ->
            ActionRow(action, SegmentedListDefaults.shapeFor(index, actions.size))
        }
    }
}

@Composable
private fun ActionRow(action: SheetAction, shape: androidx.compose.ui.graphics.Shape) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = shape, color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = action.onClick)
                .heightIn(min = 60.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (action.destructive) colors.errorContainer else colors.surfaceContainerHighest,
                contentColor = if (action.destructive) colors.onErrorContainer else colors.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
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
