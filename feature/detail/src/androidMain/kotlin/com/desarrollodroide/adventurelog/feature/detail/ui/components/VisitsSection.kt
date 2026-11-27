package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.isAllDayVisit
import com.desarrollodroide.adventurelog.core.model.visitBoundText
import com.desarrollodroide.adventurelog.core.model.visitLocalDateTime
import com.desarrollodroide.adventurelog.feature.ui.components.DateTile
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedList

private val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
private val MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "5 Jul 2026", from an ISO date or date-time; the raw value when it is not one. */
internal fun longDate(iso: String): String {
    val parts = iso.substringBefore('T').split("-")
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { MONTH_NAMES.getOrNull(it - 1) }
    val day = parts.getOrNull(2)?.toIntOrNull()
    return if (parts.size == 3 && month != null && day != null) "$day $month ${parts[0]}" else iso
}

/**
 * A place's visits as one group of rows, each with its date as a calendar sheet - the most recent
 * inked in the primary colour, as the canvas marks the one that matters. A timed visit says from
 * and to; notes follow; the visit's time zone names where it was logged.
 */
@Composable
fun VisitsSection(
    visits: List<Visit>,
    modifier: Modifier = Modifier
) {
    val latest = visits.maxByOrNull { it.startDate.orEmpty() }
    SegmentedList(items = visits, modifier = modifier) { visit -> VisitRow(visit, highlighted = visit === latest) }
}

@Composable
private fun VisitRow(visit: Visit, highlighted: Boolean) {
    val allDay = isAllDayVisit(visit.startDate, visit.endDate)
    // A timed visit is stored in UTC: its day and hours are shown in its own zone, as the web does.
    val zone = visit.timezone.takeUnless { allDay }
    val start = visitLocalDateTime(visit.startDate, zone)
    val end = visitLocalDateTime(visit.endDate, zone)
    val parts = start?.substringBefore('T')?.split("-").orEmpty()
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { MONTHS.getOrNull(it - 1) }
    val day = parts.getOrNull(2)?.toIntOrNull()

    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (month != null && day != null) DateTile(month = month, day = day, highlighted = highlighted)
        Column(Modifier.weight(1f)) {
            Text(
                text = when {
                    start == null -> "No date"
                    allDay -> longDate(start)
                    // With its time: a timed visit showed only its dates once (measured).
                    else -> "From ${visitBoundText(start)}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            val detail = listOfNotNull(
                if (allDay) "All day" else end?.let { "To ${visitBoundText(it)}" },
                visit.timezone?.takeIf { it.isNotBlank() }?.substringAfterLast('/')?.replace('_', ' '),
            ).joinToString(" · ")
            if (detail.isNotEmpty()) {
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            visit.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
