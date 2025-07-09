package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.date.*
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSection(
    formData: AdventureFormData,
    onFormDataChange: (AdventureFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var editingVisitIndex by remember { mutableStateOf<Int?>(null) }
    var tempVisitData by remember { mutableStateOf(
        VisitFormData(
            timezone = TimeZone.currentSystemDefault().id
        )
    ) }
    
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
                                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        tempVisitData = VisitFormData(
                            timezone = TimeZone.currentSystemDefault().id
                        )
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
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Only allow dates that are on or before the end date
                    return if (!tempVisitData.endDate.isNullOrEmpty()) {
                        try {
                            val endDate = LocalDate.parse(tempVisitData.endDate!!)
                            val endMillis = endDate.toEpochDays() * 24 * 60 * 60 * 1000L
                            utcTimeMillis <= endMillis
                        } catch (e: Exception) {
                            true
                        }
                    } else {
                        true
                    }
                }
            }
        )
        
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (tempVisitData.startDate.isNotEmpty()) {
                // Set initial date to start date if available
                try {
                    val startDate = LocalDate.parse(tempVisitData.startDate)
                    startDate.toEpochDays() * 24 * 60 * 60 * 1000L
                } catch (e: Exception) {
                    null
                }
            } else null,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Only allow dates that are on or after the start date
                    return if (tempVisitData.startDate.isNotEmpty()) {
                        try {
                            val startDate = LocalDate.parse(tempVisitData.startDate)
                            val startMillis = startDate.toEpochDays() * 24 * 60 * 60 * 1000L
                            utcTimeMillis >= startMillis
                        } catch (e: Exception) {
                            true
                        }
                    } else {
                        true
                    }
                }
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val selectedDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            tempVisitData = tempVisitData.copy(
                                endDate = selectedDate.toString()
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
