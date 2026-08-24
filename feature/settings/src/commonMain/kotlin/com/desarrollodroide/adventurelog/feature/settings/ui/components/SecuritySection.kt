package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SecuritySection(
    hasPassword: Boolean,
    isChangingPassword: Boolean,
    onChangePassword: (current: String, new: String, onSuccess: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogOpen by remember { mutableStateOf(false) }

    SettingsCard(
        emoji = "🔒",
        title = "Security",
        subtitle = "Update your account password",
        modifier = modifier
    ) {
        Button(
            onClick = { dialogOpen = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change password")
        }
        if (!hasPassword) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "This account signs in through an identity provider and has no password " +
                    "yet. Setting one here will let you sign in with it as well.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (dialogOpen) {
        ChangePasswordDialog(
            requiresCurrentPassword = hasPassword,
            isSaving = isChangingPassword,
            onDismiss = { dialogOpen = false },
            onConfirm = { current, new ->
                onChangePassword(current, new) { dialogOpen = false }
            }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    requiresCurrentPassword: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (current: String, new: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var reveal by remember { mutableStateOf(false) }

    val mismatch = confirmation.isNotEmpty() && confirmation != newPassword
    val canSubmit = newPassword.isNotEmpty() &&
        confirmation == newPassword &&
        (!requiresCurrentPassword || current.isNotEmpty()) &&
        !isSaving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Change password") },
        text = {
            Column {
                if (requiresCurrentPassword) {
                    PasswordField(
                        value = current,
                        onValueChange = { current = it },
                        label = "Current password",
                        reveal = reveal,
                        onToggleReveal = { reveal = !reveal }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New password",
                    reveal = reveal,
                    onToggleReveal = { reveal = !reveal }
                )
                Spacer(Modifier.height(12.dp))
                PasswordField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = "Confirm new password",
                    reveal = reveal,
                    onToggleReveal = { reveal = !reveal },
                    isError = mismatch,
                    supportingText = if (mismatch) "The passwords do not match." else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(current, newPassword) },
                enabled = canSubmit
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text("Change password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
        }
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    reveal: Boolean,
    onToggleReveal: () -> Unit,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        visualTransformation = if (reveal) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = onToggleReveal) {
                Icon(
                    imageVector = if (reveal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (reveal) "Hide password" else "Show password"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
