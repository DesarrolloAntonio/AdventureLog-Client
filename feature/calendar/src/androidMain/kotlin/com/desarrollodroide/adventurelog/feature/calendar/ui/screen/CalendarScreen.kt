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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.Earlier
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.target
import com.desarrollodroide.adventurelog.feature.ui.components.ChipTone
import com.desarrollodroide.adventurelog.feature.ui.components.MetaChip
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn

@Composable
fun CalendarScreenRoute(
    onOpenPlace: (String) -> Unit,
    onOpenCollection: (id: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<CalendarViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(
        state = state,
        onToggleType = viewModel::toggleType,
        onClearTypes = viewModel::clearTypes,
        onRetry = viewModel::load,
        onShowEarlier = viewModel::loadEarlier,
        onOpen = { target ->
            when (target) {
                is EventTarget.Place -> onOpenPlace(target.id)
                is EventTarget.Collection -> onOpenCollection(target.id, target.name)
            }
        },
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
    modifier: Modifier = Modifier,
    onShowEarlier: () -> Unit = {},
    onOpen: (EventTarget) -> Unit = {}
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

            else -> {
                val listState = rememberLazyListState()
                val items = agendaItems(state)

                // Web opens the calendar on today's month, not the start of everything the
                // account has ever logged - land on today the same way. Keyed on the date and the
                // filter, not the days: showing earlier events adds days above, and the reader who
                // asked for them is looking at the top, not at today.
                LaunchedEffect(state.today, state.selectedTypes) {
                    val index = todayScrollIndex(state)
                    if (index > 0) listState.scrollToItem(index)
                }

                // A calendar is a column of days; stretching a day across 1200dp reads worse.
                ContentColumn {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items.forEach { agendaItem ->
                            item(key = agendaItem.key) {
                                when (agendaItem) {
                                    AgendaItem.Chips -> TypeChips(state, onToggleType, onClearTypes)
                                    AgendaItem.Earlier -> EarlierRow(state, onShowEarlier)
                                    is AgendaItem.Month -> MonthHeading(agendaItem.date)
                                    is AgendaItem.Today -> TodayMarker(agendaItem.date)
                                    is AgendaItem.Day -> DayRow(
                                        day = agendaItem.day,
                                        isToday = agendaItem.day.date == state.today,
                                        onOpen = onOpen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * One row of the agenda. The list and the index it scrolls to are both read from [agendaItems], so
 * the two cannot disagree about where today is.
 */
internal sealed interface AgendaItem {
    val key: String

    data object Chips : AgendaItem {
        override val key = "chips"
    }

    data object Earlier : AgendaItem {
        override val key = "earlier"
    }

    data class Month(val date: LocalDate) : AgendaItem {
        override val key get() = "month-${monthKey(date)}"
    }

    data class Today(val date: LocalDate) : AgendaItem {
        override val key get() = "today"
    }

    data class Day(val day: CalendarDay) : AgendaItem {
        override val key get() = "day-${day.date}"
    }
}

/**
 * The agenda, top to bottom: the type chips when there is a choice, what is known before the
 * window, then each month's heading and days - with today marked where it falls, even on a date
 * with nothing on it. Nothing marked it (QA 07, CA-01): today was only used to pick where to
 * scroll, so on opening you could not tell what had happened from what was coming.
 */
internal fun agendaItems(state: CalendarUiState): List<AgendaItem> = buildList {
    if (state.availableTypes.size > 1) add(AgendaItem.Chips)
    val earlier = state.earlier
    if (state.windowStart != null && !(earlier is Earlier.Loaded && earlier.found > 0)) {
        add(AgendaItem.Earlier)
    }

    val today = state.today
    var todayPlaced = today == null
    var lastMonth: String? = null
    state.days.forEach { day ->
        val month = monthKey(day.date)
        val todayGoesHere = !todayPlaced && today != null && day.date >= today
        // Today's month has nothing in it: the marker goes between the months, dated.
        if (todayGoesHere && monthKey(today!!) != month) {
            add(AgendaItem.Today(today))
            todayPlaced = true
        }
        if (month != lastMonth) {
            lastMonth = month
            add(AgendaItem.Month(day.date))
        }
        // Today's month has something: the marker goes under its heading.
        if (todayGoesHere && !todayPlaced) {
            add(AgendaItem.Today(today!!))
            todayPlaced = true
        }
        add(AgendaItem.Day(day))
    }
    // Everything is in the past: today goes after it.
    if (!todayPlaced && today != null) add(AgendaItem.Today(today))
}

private fun monthKey(date: LocalDate) = "${date.year}-${date.monthNumber}"

/** Where the list opens: today's marker. 0 (the top) when there is no today to mark. */
internal fun todayScrollIndex(state: CalendarUiState): Int =
    agendaItems(state).indexOfFirst { it is AgendaItem.Today }.coerceAtLeast(0)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeChips(
    state: CalendarUiState,
    onToggleType: (String) -> Unit,
    onClearTypes: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val all = state.selectedTypes.isEmpty()
        MetaChip(
            text = "All",
            tone = if (all) ChipTone.ACCENT else ChipTone.NEUTRAL,
            onClick = onClearTypes,
            selected = all
        )
        state.availableTypes.forEach { type ->
            val on = type in state.selectedTypes
            MetaChip(
                text = type.replaceFirstChar { it.uppercase() },
                tone = if (on) ChipTone.ACCENT else ChipTone.NEUTRAL,
                onClick = { onToggleType(type) },
                selected = on
            )
        }
    }
}

@Composable
private fun EarlierRow(state: CalendarUiState, onShowEarlier: () -> Unit) {
    val since = state.windowStart?.let { longDate(it) } ?: return
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val earlier = state.earlier) {
            Earlier.NotAsked -> TextButton(onClick = onShowEarlier) {
                Text("Show everything before $since")
            }

            Earlier.Loading -> Row(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Looking before $since…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is Earlier.Failed -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = earlier.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = onShowEarlier) { Text("Try again") }
            }

            is Earlier.Loaded -> Text(
                text = "Nothing before $since",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun TodayMarker(date: LocalDate) {
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // In the date column, where a day's number would be.
        Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        }
        Text(
            text = "Today · ${weekdayName(date.dayOfWeek.ordinal)} ${date.dayOfMonth} ${monthName(date.monthNumber).take(3)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f).height(2.dp).background(color))
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
private fun DayRow(day: CalendarDay, isToday: Boolean, onOpen: (EventTarget) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // The date column: a number you can find with your eye, and today marked once. Sized to
        // sit centred on a card's minimum height - it was taller than a one-line card, so the
        // weekday hung below it (QA 07, screenshot).
        Column(
            modifier = Modifier.width(52.dp).padding(top = 6.dp),
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
            day.events.forEach { event -> EventCard(event, onOpen) }
        }
    }
}

@Composable
private fun EventCard(event: CalendarEvent, onOpen: (EventTarget) -> Unit) {
    val target = event.target()
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    if (target != null) {
        Card(
            onClick = { onOpen(target) },
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = elevation
        ) { EventCardContent(event) }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            colors = colors,
            elevation = elevation
        ) { EventCardContent(event) }
    }
}

@Composable
private fun EventCardContent(event: CalendarEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
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

/** "16 Sep 2025". */
private fun longDate(date: LocalDate): String =
    "${date.dayOfMonth} ${monthName(date.monthNumber).take(3)} ${date.year}"

private fun monthName(month: Int): String = when (month) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; else -> "December"
}

private fun weekdayName(ordinal: Int): String = when (ordinal) {
    0 -> "Mon"; 1 -> "Tue"; 2 -> "Wed"; 3 -> "Thu"
    4 -> "Fri"; 5 -> "Sat"; else -> "Sun"
}
