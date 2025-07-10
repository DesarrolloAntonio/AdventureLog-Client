package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSection(
    formData: CollectionFormData,
    onFormDataChange: (CollectionFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    SectionCard(
        title = "Date Information",
        icon = Icons.Outlined.CalendarMonth,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Start date
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Start date",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                DateField(
                    date = formData.startDate,
                    placeholder = "dd/mm/yyyy",
                    onDateClick = { showStartDatePicker = true }
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
                
                DateField(
                    date = formData.endDate,
                    placeholder = "dd/mm/yyyy",
                    onDateClick = { showEndDatePicker = true }
                )
            }
        }
    }
    
    // Start date picker dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Only allow dates that are on or before the end date
                    return if (formData.endDate.isNotEmpty()) {
                        try {
                            val endDate = LocalDate.parse(formData.endDate)
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
                            onFormDataChange(
                                formData.copy(
                                    startDate = localDate.toString(),
                                    endDate = if (formData.endDate.isEmpty()) localDate.toString() else formData.endDate
                                )
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
    
    // End date picker dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (formData.startDate.isNotEmpty()) {
                // Set initial date to start date if available
                try {
                    val startDate = LocalDate.parse(formData.startDate)
                    startDate.toEpochDays() * 24 * 60 * 60 * 1000L
                } catch (e: Exception) {
                    null
                }
            } else null,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Only allow dates that are on or after the start date
                    return if (formData.startDate.isNotEmpty()) {
                        try {
                            val startDate = LocalDate.parse(formData.startDate)
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
                            onFormDataChange(
                                formData.copy(endDate = selectedDate.toString())
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
}

@Composable
private fun DateField(
    date: String,
    placeholder: String,
    onDateClick: () -> Unit
) {
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
                    text = placeholder,
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
        
        // Date picker button
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