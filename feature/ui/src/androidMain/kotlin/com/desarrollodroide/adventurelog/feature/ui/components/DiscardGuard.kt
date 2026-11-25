package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditOff
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
import com.desarrollodroide.adventurelog.feature.ui.platform.PlatformBackHandler

/**
 * What a form's Cancel and Back call. With nothing typed it leaves at once; with changes it first
 * asks the question the place form asks, so a filled-in form is never thrown away without a word.
 * The collection editors used to leave straight away (QA RL-09).
 *
 * [thing] names what is being made, for the new-item title: "Discard this [thing]?".
 */
@Composable
fun rememberDiscardGuard(
    hasChanges: Boolean,
    thing: String,
    isEditing: Boolean,
    onLeave: () -> Unit
): () -> Unit {
    var confirm by remember { mutableStateOf(false) }

    PlatformBackHandler(enabled = hasChanges) { confirm = true }

    if (confirm) {
        DiscardChangesDialog(
            thing = thing,
            isEditing = isEditing,
            onDiscard = {
                confirm = false
                onLeave()
            },
            onKeepEditing = { confirm = false }
        )
    }

    return { if (hasChanges) confirm = true else onLeave() }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscardChangesDialog(thing: String, isEditing: Boolean, onDiscard: () -> Unit, onKeepEditing: () -> Unit) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        icon = {
            CookieBadge(
                icon = Icons.Outlined.EditOff,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
                size = 56.dp,
            )
        },
        title = { Text(if (isEditing) "Discard changes?" else "Discard this $thing?", textAlign = TextAlign.Center) },
        text = {
            Text(
                if (isEditing) {
                    "The changes you have made will not be saved."
                } else {
                    "Nothing has been created yet, and what you have typed will be lost."
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = onDiscard,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) { Text("Discard") }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing, shapes = ButtonDefaults.shapes()) { Text("Keep editing") }
        }
    )
}
