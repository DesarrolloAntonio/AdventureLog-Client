package com.desarrollodroide.adventurelog.feature.ui.components.date

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.common.utils.formatDateForDisplay
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * A date chosen from a calendar, with its label always in view.
 *
 * The note, checklist and stay forms asked for dates typed as "YYYY-MM-DD" into a text field whose
 * label was its placeholder - so once a date was in, nothing said which date it was ("2026-10-10",
 * "2026-10-13" side by side on the stay form), and a typo went to the server as it was (QA 04).
 *
 * [value] and what [onValueChange] receives are `yyyy-MM-dd`, or "" for no date.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    /** The first day that can be picked, `yyyy-MM-dd`: a check-out not before its check-in. */
    earliest: String? = null
) {
    var picking by remember { mutableStateOf(false) }
    val day = value.take(10)

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clickable(role = Role.Button, onClickLabel = "Choose $label") { picking = true }
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (day.isBlank()) "Not set" else formatDateForDisplay(day),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (day.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (day.isNotBlank()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear $label", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (picking) {
        fun millis(d: String?) = d?.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it.take(10)).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }.getOrNull()
        }
        val from = millis(earliest)
        val state = rememberDatePickerState(
            initialSelectedDateMillis = millis(day),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = from == null || utcTimeMillis >= from
            }
        )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    picking = false
                    // The picker's millis are that day at 00:00 UTC; read in UTC, never the device zone.
                    state.selectedDateMillis?.let {
                        onValueChange(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.toString())
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }
}
