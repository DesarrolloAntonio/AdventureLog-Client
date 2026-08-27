package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.CalendarEvent

interface CalendarRepository {
    /**
     * Dated entries between two days, each `YYYY-MM-DD`. Passing neither asks for everything the
     * account has, which for a well-used journal is a lot - the screen always asks for a window.
     */
    suspend fun getEvents(start: String?, end: String?): Either<ApiResponse, List<CalendarEvent>>
}
