package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Location

/**
 * A place's actions - [LocationActionsSheet], then the delete confirmation - for any composable
 * that shows a place (a card, a row). Shown while [visible]; [onDismiss] hides it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            icon = {
                CookieBadge(
                    icon = Icons.Outlined.Delete,
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer,
                    size = 56.dp,
                )
            },
            title = { Text("Delete place", textAlign = TextAlign.Center) },
            text = {
                Text(
                    "Are you sure you want to delete \"${location.name}\"? This action cannot be undone.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = { onDelete(); confirmDelete = false },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, shapes = ButtonDefaults.shapes()) { Text("Cancel") }
            },
        )
    }
}
