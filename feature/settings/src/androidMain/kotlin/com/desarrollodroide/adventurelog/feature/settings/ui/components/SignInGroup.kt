package com.desarrollodroide.adventurelog.feature.settings.ui.components

import com.desarrollodroide.adventurelog.feature.ui.components.settings.LocalSettingsSegmentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.EmailsSectionState
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsGroup

/**
 * Everything that decides how you get back into the account: the password, and the addresses the
 * server will write to.
 *
 * The three text buttons each address used to carry are an overflow menu now - only one of them
 * ever applies to a given address, and a row of greyed-out verbs reads like a broken screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SignInGroup(
    hasPassword: Boolean,
    isChangingPassword: Boolean,
    onChangePassword: (current: String, new: String, onSuccess: () -> Unit) -> Unit,
    emails: EmailsSectionState,
    onAddEmail: (String) -> Unit,
    onVerifyEmail: (String) -> Unit,
    onSetPrimaryEmail: (String) -> Unit,
    onRemoveEmail: (String) -> Unit,
    onRetryEmails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordDialogOpen by remember { mutableStateOf(false) }
    var addDialogOpen by remember { mutableStateOf(false) }
    var pendingRemoval by remember { mutableStateOf<String?>(null) }

    SettingsGroup(
        title = "Sign-in & email",
        modifier = modifier,
        busy = emails.isBusy,
        // Rows, as every other group: the email list is rows too, so it splits like them.
        segmented = true,
        iconContainer = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        SettingsRow(
            title = if (hasPassword) "Change password" else "Set a password",
            icon = Icons.Outlined.Key,
            supporting = if (hasPassword) {
                "The password you sign in with"
            } else {
                "This account signs in through an identity provider. Setting a password lets " +
                    "you sign in with one as well."
            },
            onClick = { passwordDialogOpen = true }
        )

        when {
            emails.isLoading && emails.addresses.isEmpty() -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LocalSettingsSegmentColor.current ?: Color.Transparent, RoundedCornerShape(6.dp))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LoadingIndicator(Modifier.size(40.dp))
                }
            }

            emails.error != null && emails.addresses.isEmpty() -> {
                SettingsRow(
                    title = "Email addresses unavailable",
                    icon = Icons.Outlined.MailOutline,
                    supporting = emails.error,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onRetryEmails,
                    showChevron = false,
                    trailing = {
                        TextButton(onClick = onRetryEmails, shapes = ButtonDefaults.shapes()) { Text("Retry") }
                    }
                )
            }

            else -> emails.addresses.forEachIndexed { index, address ->
                EmailRow(
                    address = address,
                    // The server refuses to remove the primary address, and refuses to promote an
                    // unverified one - so the menu only offers what will actually work.
                    canRemove = !address.primary && emails.addresses.size > 1,
                    enabled = !emails.isBusy,
                    onVerify = { onVerifyEmail(address.email) },
                    onSetPrimary = { onSetPrimaryEmail(address.email) },
                    onRemove = { pendingRemoval = address.email }
                )
            }
        }

        SettingsRow(
            title = "Add email address",
            icon = Icons.Outlined.Add,
            enabled = !emails.isBusy,
            showChevron = false,
            tint = MaterialTheme.colorScheme.primary,
            onClick = { addDialogOpen = true }
        )
    }

    if (passwordDialogOpen) {
        ChangePasswordDialog(
            requiresCurrentPassword = hasPassword,
            isSaving = isChangingPassword,
            onDismiss = { passwordDialogOpen = false },
            onConfirm = { current, new ->
                onChangePassword(current, new) { passwordDialogOpen = false }
            }
        )
    }

    if (addDialogOpen) {
        AddEmailDialog(
            isSaving = emails.isBusy,
            onDismiss = { addDialogOpen = false },
            onConfirm = { address ->
                addDialogOpen = false
                onAddEmail(address)
            }
        )
    }

    pendingRemoval?.let { address ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            icon = {
                DialogBadge(
                    Icons.Outlined.DeleteOutline,
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer
                )
            },
            title = { Text("Remove address?", textAlign = TextAlign.Center) },
            text = {
                Text(
                    "$address will no longer receive anything from this server.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRemoval = null
                        onRemoveEmail(address)
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }, shapes = ButtonDefaults.shapes()) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmailRow(
    address: EmailAddress,
    canRemove: Boolean,
    enabled: Boolean,
    onVerify: () -> Unit,
    onSetPrimary: () -> Unit,
    onRemove: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    val status = buildList {
        if (address.primary) add("Primary")
        add(if (address.verified) "Verified" else "Not verified")
    }.joinToString(" · ")

    SettingsRow(
        title = address.email,
        icon = Icons.Outlined.MailOutline,
        supporting = status,
        showChevron = false,
        supportingColor = if (address.verified) null else MaterialTheme.colorScheme.error,
        trailing = {
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    enabled = enabled,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Actions for ${address.email}")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    if (!address.verified) {
                        DropdownMenuItem(
                            text = { Text("Send verification email") },
                            leadingIcon = { Icon(Icons.Outlined.MarkEmailRead, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onVerify()
                            }
                        )
                    }
                    if (!address.primary && address.verified) {
                        DropdownMenuItem(
                            text = { Text("Make primary") },
                            leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onSetPrimary()
                            }
                        )
                    }
                    if (canRemove) {
                        DropdownMenuItem(
                            text = {
                                Text("Remove", color = MaterialTheme.colorScheme.error)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuOpen = false
                                onRemove()
                            }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddEmailDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var address by remember { mutableStateOf("") }
    val looksLikeAnAddress = address.contains('@') && address.substringAfter('@').contains('.')

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = { DialogBadge(Icons.Outlined.MailOutline) },
        title = { Text("Add email address", textAlign = TextAlign.Center) },
        text = {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it.trim() },
                label = { Text("Email address") },
                singleLine = true,
                supportingText = { Text("A verification email is sent straight away.") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(address) },
                enabled = looksLikeAnAddress && !isSaving,
                shapes = ButtonDefaults.shapes()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving, shapes = ButtonDefaults.shapes()) {
                Text("Cancel")
            }
        }
    )
}
