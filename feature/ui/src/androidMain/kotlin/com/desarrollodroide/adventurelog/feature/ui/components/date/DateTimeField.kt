package com.desarrollodroide.adventurelog.feature.ui.components.date

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.common.utils.formatDateForDisplay

@Composable
fun DateTimeField(
    date: String,
    time: String?,
    isAllDay: Boolean,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    if (isAllDay) {
        // For all day events, only show date field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (date.isNotEmpty()) formatDateForDisplay(date) else "",
                onValueChange = { },
                placeholder = {
                    Text(
                        text = "dd/mm/yyyy",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                enabled = false,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                )
            )
            
            // Date button
            PickerButton(Icons.Outlined.CalendarToday, "Select date", onDateClick)
        }
    } else {
        // For timed events, show combined field with two buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = if (date.isNotEmpty()) {
                    "${formatDateForDisplay(date)}, ${time ?: "--:--"}"
                } else {
                    ""
                },
                onValueChange = { },
                placeholder = {
                    Text(
                        text = "dd/mm/yyyy, --:--",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                enabled = false,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                ),
                singleLine = true
            )
            
            // Date button
            PickerButton(Icons.Outlined.CalendarToday, "Select date", onDateClick)
            
            // Time button
            PickerButton(Icons.Outlined.Schedule, "Select time", onTimeClick)
        }
    }
}

/** The filled button beside the field that opens the date or time picker. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PickerButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(22.dp))
    }
}
