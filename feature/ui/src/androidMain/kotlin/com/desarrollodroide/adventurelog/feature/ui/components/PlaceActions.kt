package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.desarrollodroide.adventurelog.core.model.Location

/**
 * A place's actions - [LocationActionsSheet], then the delete confirmation - for any composable
 * that shows a place (a card, a row). Shown while [visible]; [onDismiss] hides it.
 */
@Composable
fun PlaceActions(
    location: Location,
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onManageCollections: () -> Unit,
    onDelete: () -> Unit,
    isOwner: Boolean = true,
    onRemoveFromCollection: (() -> Unit)? = null,
    onAddPhoto: (() -> Unit)? = null,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    if (visible) {
        LocationActionsSheet(
            locationName = location.name,
            locationPlace = location.location,
            onDismiss = onDismiss,
            onOpenDetails = { onDismiss(); onOpenDetails() },
            onEdit = { onDismiss(); onEdit() },
            onDuplicate = { onDismiss(); onDuplicate() },
            onShare = { onDismiss(); onShare() },
            onManageCollections = { onDismiss(); onManageCollections() },
            onDelete = { onDismiss(); confirmDelete = true },
            isOwner = isOwner,
            onRemoveFromCollection = onRemoveFromCollection?.let { remove -> { onDismiss(); remove() } },
            onAddPhoto = onAddPhoto?.let { add -> { onDismiss(); add() } },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete place") },
            text = { Text("Are you sure you want to delete \"${location.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirmDelete = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}
