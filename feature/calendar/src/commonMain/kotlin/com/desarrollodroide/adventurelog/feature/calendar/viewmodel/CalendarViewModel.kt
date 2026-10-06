package com.desarrollodroide.adventurelog.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCalendarEventsUseCase
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class CalendarUiState(
    val days: List<CalendarDay> = emptyList(),
    val today: LocalDate? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Only the types present in the loaded window; there is no point offering a filter for none. */
    val availableTypes: List<String> = emptyList(),
    val selectedTypes: Set<String> = emptySet(),
    /** The first day the window covers. Anything before it is only asked for on request. */
    val windowStart: LocalDate? = null,
    val earlier: Earlier = Earlier.NotAsked
) {
    val isEmpty: Boolean get() = !isLoading && error == null && days.isEmpty()
}

/**
 * What is known about the journal before [CalendarUiState.windowStart].
 *
 * The window hid a year and more of visits and said nothing about it (QA 07, CA-04): three of this
 * account's visits were in the API and nowhere on the screen.
 */
sealed interface Earlier {
    data object NotAsked : Earlier
    data object Loading : Earlier
    /** How many events came back from before the window; none is an answer too. */
    data class Loaded(val found: Int) : Earlier
    data class Failed(val message: String) : Earlier
}

/** Everything happening on one date, in the order the server sorted it. */
data class CalendarDay(val date: LocalDate, val events: List<CalendarEvent>)

/** Where tapping an event goes. */
sealed interface EventTarget {
    data class Place(val id: String) : EventTarget
    data class Collection(val id: String, val name: String) : EventTarget
}

/**
 * A visit opens its place; anything else that belongs to a trip - the trip itself, a transport leg,
 * a stay, a note - opens that trip, as the web's event sheet offers "View collection". Something
 * that belongs to nothing the app can show opens nothing, rather than a screen that fails.
 *
 * Every row named something that exists elsewhere in the app and opened none of it (QA 07, CA-02).
 */
fun CalendarEvent.target(): EventTarget? {
    if (type == "visit" && resourceId.isNotBlank()) return EventTarget.Place(resourceId)
    val trip = collectionId?.takeIf { it.isNotBlank() } ?: return null
    return EventTarget.Collection(trip, collectionName.orEmpty())
}

@OptIn(ExperimentalTime::class)
class CalendarViewModel(
    private val getCalendarEventsUseCase: GetCalendarEventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private var loaded: List<CalendarEvent> = emptyList()

    init {
        load()
    }

    /**
     * A year back and two forward.
     *
     * Asking for everything is the server's default and answers with a whole journal; a window
     * covers what anyone scrolls to while keeping one request enough. What is older is one tap
     * away, at the top of the list ([loadEarlier]).
     */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val windowStart = today.minus(DatePeriod(years = 1))

            when (
                val result = getCalendarEventsUseCase(
                    start = windowStart.toString(),
                    end = today.plus(DatePeriod(years = 2)).toString()
                )
            ) {
                is Either.Left -> _uiState.update {
                    it.copy(isLoading = false, error = result.value)
                }

                is Either.Right -> {
                    loaded = result.value
                    // One update: the list scrolls to today when it first has both the days and
                    // the date, and a state with one and not the other drew a list at the top.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            today = today,
                            windowStart = windowStart,
                            earlier = Earlier.NotAsked,
                            availableTypes = typesOf(loaded),
                            days = group(loaded, it.selectedTypes)
                        )
                    }
                }
            }
        }
    }

    /**
     * Everything before the window, in one request with no lower bound. Asked for once: after it
     * the journal is complete, so there is nothing further back to offer.
     */
    fun loadEarlier() {
        val state = _uiState.value
        val windowStart = state.windowStart ?: return
        if (state.earlier == Earlier.Loading || state.earlier is Earlier.Loaded) return

        viewModelScope.launch {
            _uiState.update { it.copy(earlier = Earlier.Loading) }

            when (val result = getCalendarEventsUseCase(start = null, end = windowStart.toString())) {
                is Either.Left -> _uiState.update {
                    it.copy(earlier = Earlier.Failed(result.value))
                }

                is Either.Right -> {
                    // The server counts anything overlapping the bound, so a trip that spans the
                    // window's first day comes back in both answers.
                    val known = loaded.mapTo(HashSet()) { it.id }
                    val older = result.value.filterNot { it.id in known }
                    loaded = older + loaded
                    _uiState.update {
                        it.copy(
                            earlier = Earlier.Loaded(older.size),
                            availableTypes = typesOf(loaded),
                            days = group(loaded, it.selectedTypes)
                        )
                    }
                }
            }
        }
    }

    fun toggleType(type: String) {
        _uiState.update { state ->
            val selected = if (type in state.selectedTypes) {
                state.selectedTypes - type
            } else {
                state.selectedTypes + type
            }
            state.copy(selectedTypes = selected, days = group(loaded, selected))
        }
    }

    fun clearTypes() {
        _uiState.update { it.copy(selectedTypes = emptySet(), days = group(loaded, emptySet())) }
    }

    private fun typesOf(events: List<CalendarEvent>): List<String> = events
        .map { it.type }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    /**
     * Group by the day an event starts.
     *
     * A multi-day trip is one entry on its first day rather than one on each: a calendar that
     * repeats a fortnight's holiday fourteen times is a calendar nobody can read.
     */
    private fun group(events: List<CalendarEvent>, selected: Set<String>): List<CalendarDay> = events
        .asSequence()
        .filter { selected.isEmpty() || it.type in selected }
        .mapNotNull { event ->
            val day = event.start.substringBefore('T').takeIf { it.isNotBlank() } ?: return@mapNotNull null
            runCatching { LocalDate.parse(day) }.getOrNull()?.let { it to event }
        }
        .groupBy({ it.first }, { it.second })
        .map { (date, dayEvents) -> CalendarDay(date, dayEvents) }
        .sortedBy { it.date }
}
