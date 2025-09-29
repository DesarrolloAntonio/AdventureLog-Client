package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.SectionCard
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.date.DateTimeField
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.data.TransportationFormData

@Composable
fun DateTransportationSection(
    formData: TransportationFormData,
    onFormDataChange: (TransportationFormData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showDepartureTimePicker by remember { mutableStateOf(false) }
    var showArrivalDatePicker by remember { mutableStateOf(false) }
    var showArrivalTimePicker by remember { mutableStateOf(false) }

    SectionCard(
        title = "Date Information",
        icon = Icons.Outlined.DateRange,
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Day",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = formData.isAllDay,
                    onCheckedChange = {
                        onFormDataChange(formData.copy(isAllDay = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Constrain to Collection Dates",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = formData.constrainToCollectionDates,
                    onCheckedChange = {
                        onFormDataChange(formData.copy(constrainToCollectionDates = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Text(
                text = "Date Selection",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Departure Date",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Arrival Date",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Parse dates for display
                    val departureDate = formData.departureDate.substringBefore("T").ifEmpty { "" }
                    val departureTime = if (!formData.isAllDay && formData.departureDate.contains("T")) {
                        formData.departureDate.substringAfter("T").substringBefore("Z").take(5)
                    } else ""
                    
                    val arrivalDate = formData.arrivalDate.substringBefore("T").ifEmpty { "" }
                    val arrivalTime = if (!formData.isAllDay && formData.arrivalDate.contains("T")) {
                        formData.arrivalDate.substringAfter("T").substringBefore("Z").take(5)
                    } else ""

                    DateTimeField(
                        date = departureDate,
                        time = departureTime,
                        onDateClick = { showDepartureDatePicker = true },
                        onTimeClick = { showDepartureTimePicker = true },
                        isAllDay = formData.isAllDay
                    )

                    DateTimeField(
                        date = arrivalDate,
                        time = arrivalTime,
                        onDateClick = { showArrivalDatePicker = true },
                        onTimeClick = { showArrivalTimePicker = true },
                        isAllDay = formData.isAllDay
                    )
                }
            }
        }
    }
}
