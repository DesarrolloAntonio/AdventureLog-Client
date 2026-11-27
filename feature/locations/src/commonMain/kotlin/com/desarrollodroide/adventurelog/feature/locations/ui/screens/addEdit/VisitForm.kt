package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit

import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.model.isAllDayVisit
import com.desarrollodroide.adventurelog.core.model.visitLocalDateTime
import kotlinx.datetime.TimeZone

private data class SplitDateTime(
    val date: String,
    val time: String?
)

private fun splitIsoDateTime(isoString: String?): SplitDateTime {
    if (isoString.isNullOrBlank()) {
        return SplitDateTime(date = "", time = null)
    }
    
    return try {
        if (isoString.contains('T')) {
            val parts = isoString.split('T')
            val date = parts[0]
            val timePart = parts.getOrNull(1)?.substringBefore('+')?.substringBefore('Z') ?: ""
            val time = if (timePart.isNotEmpty()) {
                timePart.substring(0, minOf(5, timePart.length))
            } else null
            
            SplitDateTime(date = date, time = time)
        } else {
            SplitDateTime(date = isoString, time = null)
        }
    } catch (e: Exception) {
        SplitDateTime(date = isoString, time = null)
    }
}

/** A visit as the form holds it. Saving compares against this to leave untouched visits alone. */
internal fun visitFormOf(visit: Visit): VisitFormData {
    val allDay = isAllDayVisit(visit.startDate, visit.endDate)
    // A timed visit is stored in UTC: its day and hours are read in its own zone, as the web does.
    val zone = visit.timezone.takeUnless { allDay }
    val start = splitIsoDateTime(visitLocalDateTime(visit.startDate, zone))
    val end = splitIsoDateTime(visitLocalDateTime(visit.endDate, zone))
    return VisitFormData(
        id = visit.id,
        startDate = start.date,
        endDate = end.date,
        startTime = start.time.takeUnless { allDay },
        endTime = end.time.takeUnless { allDay },
        // A visit stored with no timezone used to be given Europe/Madrid, whoever was travelling.
        timezone = visit.timezone ?: TimeZone.currentSystemDefault().id,
        notes = visit.notes ?: "",
        isAllDay = allDay
    )
}
