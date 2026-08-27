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
    val selectedTypes: Set<String> = emptySet()
) {
    val isEmpty: Boolean get() = !isLoading && error == null && days.isEmpty()
}

/** Everything happening on one date, in the order the server sorted it. */
data class CalendarDay(val date: LocalDate, val events: List<CalendarEvent>)

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
     * covers what anyone scrolls to while keeping one request enough.
     */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date

            when (
                val result = getCalendarEventsUseCase(
                    start = today.minus(DatePeriod(years = 1)).toString(),
                    end = today.plus(DatePeriod(years = 2)).toString()
                )
            ) {
                is Either.Left -> _uiState.update {
                    it.copy(isLoading = false, error = result.value)
                }

                is Either.Right -> {
                    loaded = result.value
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            today = today,
                            availableTypes = loaded.map { event -> event.type }
                                .filter { type -> type.isNotBlank() }
                                .distinct()
                                .sorted()
                        )
                    }
                    regroup()
                }
            }
        }
    }

    fun toggleType(type: String) {
        _uiState.update { state ->
            val selected = state.selectedTypes
            state.copy(
                selectedTypes = if (type in selected) selected - type else selected + type
            )
        }
        regroup()
    }

    fun clearTypes() {
        _uiState.update { it.copy(selectedTypes = emptySet()) }
        regroup()
    }

    /**
     * Group by the day an event starts.
     *
     * A multi-day trip is one entry on its first day rather than one on each: a calendar that
     * repeats a fortnight's holiday fourteen times is a calendar nobody can read.
     */
    private fun regroup() {
        val selected = _uiState.value.selectedTypes
        val days = loaded
            .asSequence()
            .filter { selected.isEmpty() || it.type in selected }
            .mapNotNull { event ->
                val day = event.start.substringBefore('T').takeIf { it.isNotBlank() } ?: return@mapNotNull null
                runCatching { LocalDate.parse(day) }.getOrNull()?.let { it to event }
            }
            .groupBy({ it.first }, { it.second })
            .map { (date, events) -> CalendarDay(date, events) }
            .sortedBy { it.date }

        _uiState.update { it.copy(days = days) }
    }
}
