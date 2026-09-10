package com.desarrollodroide.adventurelog.feature.calendar

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CalendarRepository
import com.desarrollodroide.adventurelog.core.model.CalendarEvent

/**
 * Answers with whatever the test hands it, and records the window it was asked for.
 */
class CalendarRepositoryStub(
    private val answer: Either<ApiResponse, List<CalendarEvent>>
) : CalendarRepository {

    var askedStart: String? = null
        private set
    var askedEnd: String? = null
        private set
    var calls = 0
        private set

    override suspend fun getEvents(
        start: String?,
        end: String?
    ): Either<ApiResponse, List<CalendarEvent>> {
        askedStart = start
        askedEnd = end
        calls++
        return answer
    }
}

fun event(
    id: String,
    start: String,
    type: String = "visit",
    title: String = id
) = CalendarEvent(
    id = id,
    type = type,
    title = title,
    start = start,
    end = start,
    allDay = true,
    icon = "",
    category = "",
    locationLabel = "",
    collectionId = null,
    collectionName = null
)
