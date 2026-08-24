package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.EmailAddress
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.EmailsSectionState

@Composable
fun EmailsSection(
    state: EmailsSectionState,
    onNewAddressChange: (String) -> Unit,
    onAdd: () -> Unit,
    onVerify: (String) -> Unit,
    onSetPrimary: (String) -> Unit,
    onRemove: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        emoji = "📧",
        title = "Email addresses",
        subtitle = "Manage your addresses and their verification status",
        modifier = modifier
    ) {
        when {
            state.isLoading && state.addresses.isEmpty() -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            state.error != null && state.addresses.isEmpty() -> {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry) { Text("Try again") }
            }

            else -> {
                state.addresses.forEachIndexed { index, address ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    EmailRow(
                        address = address,
                        // The primary address cannot be removed, and an unverified one cannot
                        // become primary - the server enforces both, so the buttons say so.
                        canRemove = !address.primary && state.addresses.size > 1,
                        enabled = !state.isBusy,
                        onVerify = { onVerify(address.email) },
                        onSetPrimary = { onSetPrimary(address.email) },
                        onRemove = { onRemove(address.email) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.newAddress,
            onValueChange = onNewAddressChange,
            label = { Text("Add a new address") },
            singleLine = true,
            enabled = !state.isBusy,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onAdd,
            enabled = state.newAddress.isNotBlank() && !state.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Add email address")
        }
    }
}

@Composable
private fun EmailRow(
    address: EmailAddress,
    canRemove: Boolean,
    enabled: Boolean,
    onVerify: () -> Unit,
    onSetPrimary: () -> Unit,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(text = address.email, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(
                text = if (address.verified) "Verified" else "Not verified",
                positive = address.verified
            )
            if (address.primary) {
                StatusChip(text = "Primary", positive = true)
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!address.verified) {
                TextButton(onClick = onVerify, enabled = enabled) { Text("Verify") }
            }
            if (!address.primary && address.verified) {
                TextButton(onClick = onSetPrimary, enabled = enabled) { Text("Make primary") }
            }
            if (canRemove) {
                TextButton(onClick = onRemove, enabled = enabled) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, positive: Boolean) {
    val container = if (positive) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val content = if (positive) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = container,
            disabledLabelColor = content
        )
    )
}
