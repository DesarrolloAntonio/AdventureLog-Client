package com.desarrollodroide.adventurelog.feature.calendar.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarDay
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarUiState
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ChipTone
import com.desarrollodroide.adventurelog.feature.ui.components.MetaChip
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalendarScreenRoute(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<CalendarViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(
        state = state,
        onToggleType = viewModel::toggleType,
        onClearTypes = viewModel::clearTypes,
        onRetry = viewModel::load,
        modifier = modifier
    )
}

/**
 * The journal by date.
 *
 * An agenda rather than a month grid. A grid spends most of a phone's width on empty squares and
 * then cannot show what is in the full ones; a list of days that have something in them shows the
 * thing itself, and scrolls as far as the journal goes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onToggleType: (String) -> Unit,
    onClearTypes: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.days.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            state.error != null && state.days.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry) { Text("Try again") }
            }

            state.isEmpty -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nothing dated yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Visits, transport and lodging with dates on them show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (state.availableTypes.size > 1) {
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetaChip(
                                text = "All",
                                tone = if (state.selectedTypes.isEmpty()) {
                                    ChipTone.ACCENT
                                } else {
                                    ChipTone.NEUTRAL
                                },
                                onClick = onClearTypes
                            )
                            state.availableTypes.forEach { type ->
                                MetaChip(
                                    text = type.replaceFirstChar { it.uppercase() },
                                    tone = if (type in state.selectedTypes) {
                                        ChipTone.ACCENT
                                    } else {
                                        ChipTone.NEUTRAL
                                    },
                                    onClick = { onToggleType(type) }
                                )
                            }
                        }
                    }
                }

                var lastMonth: String? = null
                state.days.forEach { day ->
                    val month = "${day.date.year}-${day.date.monthNumber}"
                    if (month != lastMonth) {
                        lastMonth = month
                        item(key = "month-$month") {
                            MonthHeading(day.date)
                        }
                    }
                    item(key = "day-${day.date}") {
                        DayRow(day = day, isToday = day.date == state.today)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeading(date: LocalDate) {
    Text(
        text = "${monthName(date.monthNumber)} ${date.year}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun DayRow(day: CalendarDay, isToday: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // The date column: a number you can find with your eye, and today marked once.
        Column(
            modifier = Modifier.width(52.dp).padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Text(
                text = weekdayName(day.date.dayOfWeek.ordinal),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            day.events.forEach { EventCard(it) }
        }
    }
}

@Composable
private fun EventCard(event: CalendarEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (event.icon.isNotBlank()) {
                Text(text = event.icon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // A place inside a collection of the same name says it once, not twice.
                val detail = listOfNotNull(
                    event.locationLabel.takeIf { it.isNotBlank() },
                    event.collectionName?.takeIf { it.isNotBlank() }
                ).distinct().filterNot { it == event.title }.joinToString(" · ")
                if (detail.isNotBlank()) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = timeLabel(event),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * "All day", a clock time, or the day it runs to. A multi-day event is listed once, on the day it
 * begins, so its last day is the useful thing to print beside it.
 */
private fun timeLabel(event: CalendarEvent): String {
    val startDay = event.start.substringBefore('T')
    val endDay = event.end.substringBefore('T')
    return when {
        endDay.isNotBlank() && endDay != startDay ->
            "to ${shortDate(endDay, showYear = endDay.take(4) != startDay.take(4))}"
        event.allDay -> "All day"
        event.start.contains('T') -> event.start.substringAfter('T').take(5)
        else -> ""
    }
}

/**
 * "18 Mar", or "18 Mar 27" when the end falls in another year - a trip printed as 03/18 beneath a
 * June heading reads as a date in the past rather than one nine months away.
 */
private fun shortDate(isoDay: String, showYear: Boolean): String {
    val parts = isoDay.split('-')
    if (parts.size != 3) return isoDay
    val month = parts[1].toIntOrNull() ?: return isoDay
    val day = parts[2].toIntOrNull() ?: return isoDay
    val label = "$day ${monthName(month).take(3)}"
    return if (showYear) "$label ${parts[0].takeLast(2)}" else label
}

private fun monthName(month: Int): String = when (month) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; else -> "December"
}

private fun weekdayName(ordinal: Int): String = when (ordinal) {
    0 -> "Mon"; 1 -> "Tue"; 2 -> "Wed"; 3 -> "Thu"
    4 -> "Fri"; 5 -> "Sat"; else -> "Sun"
}
