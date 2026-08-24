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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.MapStyles
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileForm
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.ProfileSectionState

/**
 * Mirrors the web's Profile section. Everything here is stored on the server, so it follows the
 * account to any other client.
 *
 * The profile picture is deliberately absent: it is a multipart upload on the same endpoint and
 * needs the image pipeline, which is tracked separately.
 */
@Composable
fun ProfileSection(
    state: ProfileSectionState,
    onEdit: ((ProfileForm) -> ProfileForm) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsCard(
        emoji = "👤",
        title = "Profile",
        subtitle = "Your details, as everyone else sees them",
        modifier = modifier
    ) {
        val form = state.form

        OutlinedTextField(
            value = form.username,
            onValueChange = { value -> onEdit { it.copy(username = value) } },
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = form.firstName,
            onValueChange = { value -> onEdit { it.copy(firstName = value) } },
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
            value = form.lastName,
            onValueChange = { value -> onEdit { it.copy(lastName = value) } },
            label = { Text("Last name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))

        ToggleRow(
            title = "Public profile",
            description = "Make your profile visible to other users",
            checked = form.publicProfile,
            onCheckedChange = { value -> onEdit { it.copy(publicProfile = value) } }
        )
        ToggleRow(
            title = "Use imperial units",
            description = "Feet, inches and pounds instead of metric units",
            checked = form.imperialUnits,
            onCheckedChange = { value -> onEdit { it.copy(imperialUnits = value) } }
        )

        Spacer(Modifier.height(12.dp))
        SettingsDropdown(
            label = "Preferred currency",
            selected = form.currency,
            entries = Currencies.options.map { (code, name) ->
                DropdownEntry(value = code, label = "$code - $name")
            },
            onSelect = { value -> onEdit { it.copy(currency = value) } },
            helperText = "Pre-fills the money fields when you add something new."
        )
        Spacer(Modifier.height(12.dp))
        SettingsDropdown(
            label = "Default map style",
            selected = form.mapStyle,
            entries = MapStyles.options.map { option ->
                DropdownEntry(value = option.code, label = option.label, group = option.group)
            },
            onSelect = { value -> onEdit { it.copy(mapStyle = value) } },
            helperText = "Used as the base map everywhere. The phone draws the closest " +
                "Google Maps equivalent."
        )

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.hasChanges && !state.isSaving) {
                TextButton(onClick = onDiscard) { Text("Discard") }
                Spacer(Modifier.size(8.dp))
            }
            Button(
                onClick = onSave,
                enabled = state.hasChanges && !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text("Update")
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
