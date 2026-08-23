package com.desarrollodroide.adventurelog.feature.collections.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection

/**
 * The actions available on a collection, matching what the web offers.
 *
 * A sheet for the same reason the location one is: a dropdown anchored to the overflow button
 * moves depending on where the card sits, and eight entries do not fit comfortably in one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionActionsSheet(
    collection: UltraSlimCollection,
    busyLabel: String?,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onArchive: () -> Unit,
    onDownloadPdf: () -> Unit,
    onExportZip: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            SheetHeader(collection)

            if (busyLabel != null) {
                BusyRow(busyLabel)
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ActionRow(Icons.Outlined.Info, "Open collection", onOpen)
            ActionRow(Icons.Outlined.Edit, "Edit collection", onEdit)
            ActionRow(Icons.Outlined.Share, "Share externally", onShare)
            ActionRow(Icons.Outlined.ContentCopy, "Duplicate", onDuplicate)
            ActionRow(Icons.Outlined.PictureAsPdf, "Download PDF", onDownloadPdf)
            ActionRow(Icons.Outlined.FolderZip, "Export ZIP", onExportZip)
            ActionRow(
                icon = if (collection.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                label = if (collection.isArchived) "Restore from archive" else "Archive",
                onClick = onArchive
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            ActionRow(
                icon = Icons.Outlined.Delete,
                label = "Delete",
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SheetHeader(collection: UltraSlimCollection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = collection.name.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${collection.adventureCount} " +
                    if (collection.adventureCount == 1) "place" else "places",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Exports take a moment to render server-side, so the sheet says what it is waiting for. */
@Composable
private fun BusyRow(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint
        )
        Spacer(Modifier.width(18.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}
