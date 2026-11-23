package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.components.filters.FilterSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.date.DateTimeField
import com.desarrollodroide.adventurelog.feature.ui.components.date.TimePickerDialog
import com.desarrollodroide.adventurelog.feature.ui.components.date.TimezoneDropdown
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
@Composable
fun DateSection(
    formData: LocationFormData,
    onFormDataChange: (LocationFormData) -> Unit
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
            // Settings: the timezone and whether the visit is all day.
            FilterSection("Settings") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Timezone dropdown
                    TimezoneDropdown(
                        selectedTimezone = tempVisitData.timezone,
                        onTimezoneSelected = { timezone ->
                            tempVisitData = tempVisitData.copy(timezone = timezone)
                        }
                    )

                    // All day switch: the whole row toggles it, not only the switch.
                    AllDayRow(
                        checked = tempVisitData.isAllDay,
                        onCheckedChange = { isAllDay ->
                            tempVisitData = tempVisitData.copy(isAllDay = isAllDay)
                        }
                    )
                }
            }

            // Start date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Start date",
                    style = MaterialTheme.typography.labelLarge,
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
                    style = MaterialTheme.typography.labelLarge,
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
                        style = MaterialTheme.typography.labelLarge,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default
                    ),
                    // The form's own field: outlined on the card, as StyledTextField draws them.
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Add/Update button
            val isFormValid = tempVisitData.startDate.isNotEmpty() &&
                    (tempVisitData.isAllDay || (!tempVisitData.startTime.isNullOrEmpty() && !tempVisitData.endTime.isNullOrEmpty()))

            PrimaryButton(
                onClick = {
                    if (isFormValid) {
                        onFormDataChange(
                            formData.copy(visits = formData.visits.withVisitSaved(editingVisitIndex, tempVisitData))
                        )

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
            FilterSection("Visits") {
                if (formData.visits.isEmpty()) {
                    Text(
                        text = "No visits added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(SegmentedListDefaults.Gap)) {
                        formData.visits.forEachIndexed { index, visit ->
                            VisitItem(
                                visit = visit,
                                shape = SegmentedListDefaults.shapeFor(index, formData.visits.size),
                                onEdit = {
                                    tempVisitData = visit
                                    editingVisitIndex = index
                                },
                                onDelete = {
                                    val updatedVisits = formData.visits.toMutableList().apply {
                                        removeAt(index)
                                    }
                                    val stillEditing = editingVisitIndex?.let { editingIndexAfterDelete(it, index) }
                                    if (editingVisitIndex != null && stillEditing == null) {
                                        // The visit open above is gone: Update would bring it back.
                                        tempVisitData = VisitFormData(timezone = TimeZone.currentSystemDefault().id)
                                    }
                                    editingVisitIndex = stillEditing
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
        val initialDateMillis = if (tempVisitData.startDate.isNotEmpty()) {
            try {
                val date = LocalDate.parse(tempVisitData.startDate)
                date.toEpochDays() * 24 * 60 * 60 * 1000L
            } catch (e: Exception) {
                null
            }
        } else null
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
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
                Button(
                    shapes = ButtonDefaults.shapes(),
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
                TextButton(onClick = { showStartDatePicker = false }, shapes = ButtonDefaults.shapes()) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val initialDateMillis = if (!tempVisitData.endDate.isNullOrEmpty()) {
            try {
                val date = LocalDate.parse(tempVisitData.endDate!!)
                date.toEpochDays() * 24 * 60 * 60 * 1000L
            } catch (e: Exception) {
                if (tempVisitData.startDate.isNotEmpty()) {
                    try {
                        val startDate = LocalDate.parse(tempVisitData.startDate)
                        startDate.toEpochDays() * 24 * 60 * 60 * 1000L
                    } catch (e: Exception) {
                        null
                    }
                } else null
            }
        } else if (tempVisitData.startDate.isNotEmpty()) {
            try {
                val startDate = LocalDate.parse(tempVisitData.startDate)
                startDate.toEpochDays() * 24 * 60 * 60 * 1000L
            } catch (e: Exception) {
                null
            }
        } else null
        
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
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
                Button(
                    shapes = ButtonDefaults.shapes(),
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
                TextButton(onClick = { showEndDatePicker = false }, shapes = ButtonDefaults.shapes()) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialogs
    if (showStartTimePicker) {
        val (initialHour, initialMinute) = if (!tempVisitData.startTime.isNullOrEmpty()) {
            try {
                val parts = tempVisitData.startTime!!.split(":")
                parts[0].toInt() to parts[1].toInt()
            } catch (e: Exception) {
                0 to 0
            }
        } else {
            0 to 0
        }

        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                tempVisitData = tempVisitData.copy(
                    startTime = "${hour.toString().padStart(2, '0')}:${
                        minute.toString().padStart(2, '0')
                    }"
                )
                showStartTimePicker = false
            },
            initialHour = initialHour,
            initialMinute = initialMinute
        )
    }
    
    if (showEndTimePicker) {
        val (initialHour, initialMinute) = if (!tempVisitData.endTime.isNullOrEmpty()) {
            try {
                val parts = tempVisitData.endTime!!.split(":")
                parts[0].toInt() to parts[1].toInt()
            } catch (e: Exception) {
                0 to 0
            }
        } else {
            0 to 0
        }

        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                tempVisitData = tempVisitData.copy(
                    endTime = "${hour.toString().padStart(2, '0')}:${
                        minute.toString().padStart(2, '0')
                    }"
                )
                showEndTimePicker = false
            },
            initialHour = initialHour,
            initialMinute = initialMinute
        )
    }
}

/** "All day" as a settings row on the card; the whole row toggles it. */
@Composable
private fun AllDayRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch)
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.WbSunny, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Text(
                text = "All day",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else null
            )
        }
    }
}
