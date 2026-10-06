package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportDates
import com.desarrollodroide.adventurelog.feature.ui.components.date.TimePickerDialog
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
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
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.SectionCard
import com.desarrollodroide.adventurelog.feature.ui.components.date.DateTimeField

@Composable
fun DateTransportationSection(
    formData: TransportationFormData,
    onFormDataChange: (TransportationFormData) -> Unit,
    /** The collection's dates. "Constrain to Collection Dates" only appears when there are both. */
    collectionStart: String? = null,
    collectionEnd: String? = null
) {
    val collectionRange = if (collectionStart != null && collectionEnd != null) {
        collectionStart.take(10) to collectionEnd.take(10)
    } else {
        null
    }
    // Days the pickers offer: inside the collection when asked to be.
    val allowedRange = collectionRange?.takeIf { formData.constrainToCollectionDates }
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

            // The rows are the switches (as CO-11): a tap on the words did nothing.
            SwitchRow(
                title = "All Day",
                checked = formData.isAllDay,
                onCheckedChange = { allDay ->
                    onFormDataChange(
                        formData.copy(
                            isAllDay = allDay,
                            departureDate = TransportDates.switchAllDay(formData.departureDate, formData.departureTimezone, allDay),
                            arrivalDate = TransportDates.switchAllDay(formData.arrivalDate, formData.arrivalTimezone, allDay)
                        )
                    )
                }
            )

            if (collectionRange != null) {
                SwitchRow(
                    title = "Constrain to Collection Dates",
                    checked = formData.constrainToCollectionDates,
                    onCheckedChange = { onFormDataChange(formData.copy(constrainToCollectionDates = it)) }
                )
            }

            Text(
                text = "Date Selection",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // One above the other. Side by side, each field took the whole width and the arrival
            // was squeezed out of the card with its buttons piled on top of each other (QA 04).
            val departure = TransportDates.toLocal(formData.departureDate, formData.departureTimezone, formData.isAllDay)
            val arrival = TransportDates.toLocal(formData.arrivalDate, formData.arrivalTimezone, formData.isAllDay)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Departure", style = MaterialTheme.typography.bodyMedium)
                DateTimeField(
                    date = departure.date,
                    time = departure.time.orEmpty(),
                    onDateClick = { showDepartureDatePicker = true },
                    onTimeClick = { showDepartureTimePicker = true },
                    isAllDay = formData.isAllDay
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Arrival", style = MaterialTheme.typography.bodyMedium)
                DateTimeField(
                    date = arrival.date,
                    time = arrival.time.orEmpty(),
                    onDateClick = { showArrivalDatePicker = true },
                    onTimeClick = { showArrivalTimePicker = true },
                    isAllDay = formData.isAllDay
                )
            }
        }
    }

    // QA 04, CO-06: the four flags above were set and nothing ever read them, so a transport's
    // dates could not be set from the app at all.
    if (showDepartureDatePicker) {
        TransportDatePicker(
            initial = TransportDates.toLocal(formData.departureDate, formData.departureTimezone, formData.isAllDay).date,
            earliest = allowedRange?.first,
            latest = allowedRange?.second,
            onDismiss = { showDepartureDatePicker = false },
            onPicked = { day ->
                showDepartureDatePicker = false
                val time = TransportDates.toLocal(formData.departureDate, formData.departureTimezone, formData.isAllDay).time
                val departure = TransportDates.toStored(day, time, formData.departureTimezone, formData.isAllDay)
                onFormDataChange(
                    formData.copy(
                        departureDate = departure,
                        // An arrival nobody set yet starts on the same day, like the collection form.
                        // An arrival nobody set yet, or one now before the departure, moves with it.
                        arrivalDate = formData.arrivalDate
                            .takeUnless { it.isBlank() || TransportDates.arrivesBeforeDeparting(departure, it) }
                            ?: departure
                    )
                )
            }
        )
    }
    if (showArrivalDatePicker) {
        TransportDatePicker(
            initial = TransportDates.toLocal(formData.arrivalDate, formData.arrivalTimezone, formData.isAllDay).date,
            // Not before the departure's day.
            earliest = listOfNotNull(
                allowedRange?.first,
                TransportDates.toLocal(formData.departureDate, formData.departureTimezone, formData.isAllDay).date.ifBlank { null }
            ).maxOrNull(),
            latest = allowedRange?.second,
            onDismiss = { showArrivalDatePicker = false },
            onPicked = { day ->
                showArrivalDatePicker = false
                val time = TransportDates.toLocal(formData.arrivalDate, formData.arrivalTimezone, formData.isAllDay).time
                onFormDataChange(
                    formData.copy(arrivalDate = TransportDates.toStored(day, time, formData.arrivalTimezone, formData.isAllDay))
                )
            }
        )
    }
    if (showDepartureTimePicker) {
        val local = TransportDates.toLocal(formData.departureDate, formData.departureTimezone, allDay = false)
        TransportTimePicker(
            initial = local.time,
            onDismiss = { showDepartureTimePicker = false },
            onPicked = { time ->
                showDepartureTimePicker = false
                val day = local.date.ifBlank { todayIn(formData.departureTimezone) }
                onFormDataChange(
                    formData.copy(departureDate = TransportDates.toStored(day, time, formData.departureTimezone, allDay = false))
                )
            }
        )
    }
    if (showArrivalTimePicker) {
        val local = TransportDates.toLocal(formData.arrivalDate, formData.arrivalTimezone, allDay = false)
        TransportTimePicker(
            initial = local.time,
            onDismiss = { showArrivalTimePicker = false },
            onPicked = { time ->
                showArrivalTimePicker = false
                val day = local.date.ifBlank {
                    TransportDates.toLocal(formData.departureDate, formData.departureTimezone, allDay = false).date
                        .ifBlank { todayIn(formData.arrivalTimezone) }
                }
                onFormDataChange(
                    formData.copy(arrivalDate = TransportDates.toStored(day, time, formData.arrivalTimezone, allDay = false))
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun TransportDatePicker(
    initial: String,
    earliest: String? = null,
    latest: String? = null,
    onDismiss: () -> Unit,
    onPicked: (String) -> Unit
) {
    fun millis(day: String?) = day?.takeIf { it.isNotBlank() }?.let {
        runCatching { LocalDate.parse(it.take(10)).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }.getOrNull()
    }
    val from = millis(earliest)
    val to = millis(latest)
    val state = rememberDatePickerState(
        initialSelectedDateMillis = millis(initial),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                (from == null || utcTimeMillis >= from) && (to == null || utcTimeMillis <= to)
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    // The picker's millis are that day at 00:00 UTC; read them in UTC, or a zone
                    // behind Greenwich lands on the day before.
                    state.selectedDateMillis?.let {
                        onPicked(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.toString())
                    } ?: onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun TransportTimePicker(initial: String?, onDismiss: () -> Unit, onPicked: (String) -> Unit) {
    val parts = initial?.split(":")?.mapNotNull { it.toIntOrNull() }
    TimePickerDialog(
        onDismiss = onDismiss,
        onConfirm = { hour, minute ->
            onPicked("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
        },
        initialHour = parts?.getOrNull(0) ?: 0,
        initialMinute = parts?.getOrNull(1) ?: 0
    )
}

@OptIn(ExperimentalTime::class)
private fun todayIn(zone: String): String =
    kotlin.time.Clock.System.now().toLocalDateTime(runCatching { TimeZone.of(zone) }.getOrDefault(TimeZone.UTC)).date.toString()

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
