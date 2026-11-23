package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileForm

/**
 * The three free-text fields of the profile, opened deliberately rather than left live on the
 * screen. Everything here is one PATCH, so it saves as one action.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditProfileDialog(
    initial: ProfileForm,
    isSaving: Boolean,
    error: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (username: String, firstName: String, lastName: String) -> Unit
) {
    var username by remember { mutableStateOf(initial.username) }
    var firstName by remember { mutableStateOf(initial.firstName) }
    var lastName by remember { mutableStateOf(initial.lastName) }

    val blankUsername = username.isBlank()
    val changed = username != initial.username ||
        firstName != initial.firstName ||
        lastName != initial.lastName

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = { DialogBadge(Icons.Outlined.Person) },
        title = { Text("Edit profile", textAlign = TextAlign.Center) },
        text = {
            Column {
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(Icons.Default.AlternateEmail, contentDescription = null)
                    },
                    singleLine = true,
                    isError = blankUsername,
                    supportingText = {
                        Text(
                            text = if (blankUsername) {
                                "A username is required."
                            } else {
                                "How other users find you."
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username.trim(), firstName.trim(), lastName.trim()) },
                enabled = changed && !blankUsername && !isSaving,
                shapes = ButtonDefaults.shapes()
            ) {
                if (isSaving) {
                    LoadingIndicator(modifier = Modifier.size(24.dp), color = LocalContentColor.current)
                    Spacer(Modifier.size(8.dp))
                }
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving, shapes = ButtonDefaults.shapes()) {
                Text("Cancel")
            }
        }
    )
}

/** The badge a settings dialog opens with: its subject's icon on a tonal cookie. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DialogBadge(
    icon: ImageVector,
    container: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        Modifier
            .size(56.dp)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(28.dp))
    }
}
