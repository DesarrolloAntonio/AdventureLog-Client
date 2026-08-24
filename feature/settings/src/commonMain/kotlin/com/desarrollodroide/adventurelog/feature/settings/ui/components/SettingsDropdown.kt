package com.desarrollodroide.adventurelog.feature.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A single choice out of a list.
 *
 * Written as a row plus a menu rather than an ExposedDropdownMenuBox because the map style list
 * runs to 25 entries in five groups, and a plain menu is the only one of the two that can carry
 * group headings and still scroll.
 */
data class DropdownEntry(
    val value: String,
    val label: String,
    /** Drawn as a non-selectable heading above this entry when it opens a new group. */
    val group: String? = null
)

@Composable
fun SettingsDropdown(
    label: String,
    selected: String,
    entries: List<DropdownEntry>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = entries.firstOrNull { it.value == selected }?.label ?: selected

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                var lastGroup: String? = null
                entries.forEach { entry ->
                    if (entry.group != null && entry.group != lastGroup) {
                        lastGroup = entry.group
                        Text(
                            text = entry.group,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp
                            )
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(entry.label) },
                        onClick = {
                            expanded = false
                            onSelect(entry.value)
                        },
                        trailingIcon = if (entry.value == selected) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
        if (helperText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
