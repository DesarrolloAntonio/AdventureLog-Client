package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CalendarRepository
import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class CalendarRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : CalendarRepository {

    override suspend fun getEvents(
        start: String?,
        end: String?
    ): Either<ApiResponse, List<CalendarEvent>> {
        return try {
            val events = networkDataSource.getCalendarEvents(start, end)
                .events
                .map { it.toDomainModel() }
            Either.Right(events)
        } catch (e: HttpException) {
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            Either.Left(ApiResponse.HttpError)
        }
    }
}
