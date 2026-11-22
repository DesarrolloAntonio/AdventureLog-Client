package com.desarrollodroide.adventurelog.feature.calendar.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarDay
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarUiState
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** The calendar's two ways of looking: the agenda list it always had, or a month as a grid. */
enum class CalendarMode { AGENDA, MONTH }

/** The month's days in a Monday-first grid: nulls before the 1st and after the last, whole weeks. */
fun monthCells(year: Int, month: Int): List<LocalDate?> {
    val first = LocalDate(year, month, 1)
    val next = first.plus(DatePeriod(months = 1))
    val length = (next.toEpochDays() - first.toEpochDays()).toInt()
    val lead = first.dayOfWeek.ordinal // Monday = 0
    val cells = List(lead) { null } + (0 until length).map { first.plus(DatePeriod(days = it)) }
    val trail = (7 - cells.size % 7) % 7
    return cells + List(trail) { null }
}

private fun isoDay(value: String): LocalDate? =
    runCatching { LocalDate.parse(value.substringBefore('T')) }.getOrNull()

/** The days covered by events that run over more than one day - a trip, a stay - drawn as a band. */
fun spannedDays(days: List<CalendarDay>): Set<LocalDate> = buildSet {
    days.flatMap { it.events }.forEach { event ->
        val start = isoDay(event.start) ?: return@forEach
        val end = isoDay(event.end) ?: return@forEach
        var day = start
        while (day <= end && end > start) {
            add(day)
            day = day.plus(DatePeriod(days = 1))
        }
    }
}

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

/** Agenda | Month, as a connected pair of toggle buttons. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarModeToggle(mode: CalendarMode, onMode: (CalendarMode) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        listOf(CalendarMode.AGENDA to "Agenda", CalendarMode.MONTH to "Month").forEachIndexed { index, (value, label) ->
            ToggleButton(
                checked = mode == value,
                onCheckedChange = { if (it) onMode(value) },
                shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes()
                else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                colors = ToggleButtonDefaults.colors(
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurfaceVariant,
                    checkedContainerColor = colors.inverseSurface,
                    checkedContentColor = colors.inverseOnSurface,
                ),
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
            ) { Text(label, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

/**
 * A month as the canvas draws it: the days in a grid, a trip as a band across the days it covers,
 * today inked, and under the grid the chosen day's events, which open as they do in the agenda.
 */
@Composable
fun MonthView(state: CalendarUiState, onOpen: (EventTarget) -> Unit, modifier: Modifier = Modifier) {
    val start = state.today ?: state.days.firstOrNull()?.date ?: LocalDate(2026, 1, 1)
    var year by rememberSaveable { mutableIntStateOf(start.year) }
    var month by rememberSaveable { mutableIntStateOf(start.month.ordinal + 1) }
    var selected by rememberSaveable { mutableStateOf(state.today?.toString()) }

    val byDate = state.days.associateBy { it.date }
    val spans = spannedDays(state.days)
    val colors = MaterialTheme.colorScheme

    fun shift(by: Int) {
        val moved = LocalDate(year, month, 1).let { if (by > 0) it.plus(DatePeriod(months = by)) else it.minus(DatePeriod(months = -by)) }
        year = moved.year
        month = moved.month.ordinal + 1
    }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 120.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("${MONTH_NAMES[month - 1]} $year", style = AdventureType.display(28.sp), modifier = Modifier.weight(1f))
            FilledTonalIconButton(onClick = { shift(-1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            FilledTonalIconButton(onClick = { shift(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach {
                Text(
                    it, modifier = Modifier.weight(1f).clearAndSetSemantics { }, textAlign = TextAlign.Center,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = colors.onSurfaceVariant,
                )
            }
        }
        monthCells(year, month).chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (date != null) {
                            val events = byDate[date]?.events.orEmpty()
                            val inSpan = date in spans
                            val isToday = date == state.today
                            val isSelected = date.toString() == selected
                            // The band: the trip's days joined across the row, rounded where it starts and ends.
                            if (inSpan) {
                                val prev = date.minus(DatePeriod(days = 1)) in spans && date.dayOfWeek.ordinal != 0
                                val next = date.plus(DatePeriod(days = 1)) in spans && date.dayOfWeek.ordinal != 6
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = if (prev) 0.dp else 17.dp, bottomStart = if (prev) 0.dp else 17.dp,
                                                topEnd = if (next) 0.dp else 17.dp, bottomEnd = if (next) 0.dp else 17.dp,
                                            )
                                        )
                                        .background(colors.primaryContainer)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) colors.primary else Color.Transparent)
                                    .then(if (isSelected && !isToday) Modifier.border(2.dp, colors.primary, CircleShape) else Modifier)
                                    .clickable { selected = date.toString() }
                                    .semantics {
                                        contentDescription = "${date.day} ${MONTH_NAMES[month - 1]}" +
                                            if (events.isEmpty()) "" else ", ${events.size} ${if (events.size == 1) "event" else "events"}"
                                    },
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    date.day.toString(),
                                    fontWeight = if (events.isNotEmpty() || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isToday -> colors.onPrimary
                                        inSpan -> colors.onPrimaryContainer
                                        else -> colors.onSurface
                                    },
                                )
                                if (events.isNotEmpty()) {
                                    Box(Modifier.size(5.dp).clip(CircleShape).background(if (isToday) colors.onPrimary else colors.primary))
                                }
                            }
                        }
                    }
                }
            }
        }

        val day = selected?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        Spacer(Modifier.height(16.dp))
        if (day != null) {
            Text(
                text = "${day.day} ${MONTH_NAMES[day.month.ordinal]}",
                style = AdventureType.display(24.sp),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            val events = byDate[day]?.events.orEmpty()
            if (events.isEmpty()) {
                Text("Nothing on this day.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { events.forEach { EventCard(it, onOpen) } }
            }
        }
    }
}
