package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var editingVisitIndex by remember { mutableStateOf<Int?>(null) }
    var tempVisitData by remember { mutableStateOf(VisitFormData()) }
    
    SectionCard(
        title = "Date information",
        icon = Icons.Outlined.CalendarToday,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Timezone dropdown
                    TimezoneDropdown(
                        selectedTimezone = tempVisitData.timezone,
                        onTimezoneSelected = { timezone ->
                            tempVisitData = tempVisitData.copy(timezone = timezone)
                        }
                    )
                    
                    // All day switch
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All day",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = tempVisitData.isAllDay,
                                onCheckedChange = { isAllDay ->
                                    tempVisitData = tempVisitData.copy(isAllDay = isAllDay)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
            
            // Start date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Start date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                DateTimeField(
                    date = tempVisitData.startDate,
                    time = if (tempVisitData.isAllDay) null else tempVisitData.startTime,
                    isAllDay = tempVisitData.isAllDay,
                    onDateClick = { showStartDatePicker = true },
                    onTimeClick = { showStartTimePicker = true }
                )
            }
            
            // End date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "End date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                DateTimeField(
                    date = tempVisitData.endDate ?: tempVisitData.startDate,
                    time = if (tempVisitData.isAllDay) null else tempVisitData.endTime,
                    isAllDay = tempVisitData.isAllDay,
                    onDateClick = { showEndDatePicker = true },
                    onTimeClick = { showEndTimePicker = true }
                )
            }
            
            // Add notes - using the same style as DescriptionSection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add notes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                OutlinedTextField(
                    value = tempVisitData.notes,
                    onValueChange = { notes ->
                        tempVisitData = tempVisitData.copy(notes = notes)
                    },
                    placeholder = { 
                        Text(
                            text = "Add notes about this visit",
                            color = Color.Gray
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            
            // Add/Update button
            val isFormValid = tempVisitData.startDate.isNotEmpty() && 
                (tempVisitData.isAllDay || (!tempVisitData.startTime.isNullOrEmpty() && !tempVisitData.endTime.isNullOrEmpty()))
            
            PrimaryButton(
                onClick = {
                    if (isFormValid) {
                        val updatedVisits = if (editingVisitIndex != null) {
                            formData.visits.toMutableList().apply {
                                set(editingVisitIndex!!, tempVisitData)
                            }
                        } else {
                            formData.visits + tempVisitData
                        }
                        onFormDataChange(formData.copy(visits = updatedVisits))
                        
                        // Reset form
                        tempVisitData = VisitFormData()
                        editingVisitIndex = null
                    }
                },
                text = if (editingVisitIndex != null) "Update" else "Add",
                enabled = isFormValid
            )
            
            // Visits section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Visits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (formData.visits.isEmpty()) {
                        Text(
                            text = "No visits added yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        formData.visits.forEachIndexed { index, visit ->
                            VisitItem(
                                visit = visit,
                                onEdit = {
                                    tempVisitData = visit
                                    editingVisitIndex = index
                                },
                                onDelete = {
                                    val updatedVisits = formData.visits.toMutableList().apply {
                                        removeAt(index)
                                    }
                                    onFormDataChange(formData.copy(visits = updatedVisits))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Date picker dialogs
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            tempVisitData = tempVisitData.copy(
                                startDate = localDate.toString(),
                                endDate = if (tempVisitData.endDate.isNullOrEmpty()) localDate.toString() else tempVisitData.endDate
                            )
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            tempVisitData = tempVisitData.copy(
                                endDate = localDate.toString()
                            )
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                tempVisitData = tempVisitData.copy(
                    startTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                )
                showStartTimePicker = false
            }
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                tempVisitData = tempVisitData.copy(
                    endTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                )
                showEndTimePicker = false
            }
        )
    }
}

@Composable
private fun DateTimeField(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        TimePicker(state = timePickerState)
    }
}

@Composable
private fun TimezoneDropdown(
    selectedTimezone: String,
    onTimezoneSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val commonTimezones = listOf(
        "Europe/Madrid" to "Europe/Madrid",
        "America/New_York" to "America/New York",
        "America/Los_Angeles" to "America/Los Angeles",
        "America/Chicago" to "America/Chicago",
        "Europe/London" to "Europe/London",
        "Europe/Paris" to "Europe/Paris",
        "Europe/Berlin" to "Europe/Berlin",
        "Asia/Tokyo" to "Asia/Tokyo",
        "Asia/Shanghai" to "Asia/Shanghai",
        "Australia/Sydney" to "Australia/Sydney",
        "UTC" to "UTC"
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Timezone",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Box {
            StyledTextField(
                value = selectedTimezone,
                onValueChange = { },
                label = "Select timezone",
                icon = Icons.Outlined.Schedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                commonTimezones.forEach { (value, display) ->
                    DropdownMenuItem(
                        text = { Text(display) },
                        onClick = {
                            onTimezoneSelected(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitItem(
    visit: VisitFormData,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start date and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (visit.isAllDay) {
                        formatDateForDisplay(visit.startDate)
                    } else {
                        "${formatDateForDisplay(visit.startDate)}, ${visit.startTime ?: "00:00"}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // End date and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "End",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (visit.isAllDay) {
                        formatDateForDisplay(visit.endDate ?: visit.startDate)
                    } else {
                        "${formatDateForDisplay(visit.endDate ?: visit.startDate)}, ${visit.endTime ?: "00:00"}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Timezone and all day badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = visit.timezone,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (visit.isAllDay) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "All day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            
            // Notes if present
            if (visit.notes.isNotEmpty()) {
                Text(
                    text = visit.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

private fun formatDateForDisplay(dateString: String): String {
    return try {
        if (dateString.isEmpty()) return ""
        
        // Parse ISO date string (yyyy-MM-dd) and format to dd/mm/yyyy
        val parts = dateString.split("-")
        if (parts.size == 3) {
            "${parts[2].padStart(2, '0')}/${parts[1].padStart(2, '0')}/${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}