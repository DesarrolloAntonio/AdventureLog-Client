package com.desarrollodroide.adventurelog.feature.ui.components.date

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                enabled = false,
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = Color.Transparent,
                    disabledPlaceholderColor = Color.Gray,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
            
            // Date button
            IconButton(
                onClick = onDateClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
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
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                enabled = false,
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = Color.Transparent,
                    disabledPlaceholderColor = Color.Gray,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                singleLine = true
            )
            
            // Date button
            IconButton(
                onClick = onDateClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "Select date",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // Time button
            IconButton(
                onClick = onTimeClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Select time",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
