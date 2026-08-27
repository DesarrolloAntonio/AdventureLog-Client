package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CalendarRepository
import com.desarrollodroide.adventurelog.core.model.CalendarEvent

class GetCalendarEventsUseCase(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(
        start: String? = null,
        end: String? = null
    ): Either<String, List<CalendarEvent>> {
        return when (val result = calendarRepository.getEvents(start, end)) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> "No internet connection."
                    is ApiResponse.HttpError -> "Could not load your calendar. Please try again."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                }
            )
            is Either.Right -> Either.Right(result.value)
        }
    }
}
