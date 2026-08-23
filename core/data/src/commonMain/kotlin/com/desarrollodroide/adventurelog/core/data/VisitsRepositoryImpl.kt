package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.VisitsRepository
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class VisitsRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : VisitsRepository {

    override suspend fun createVisit(
        locationId: String,
        visit: VisitFormData
    ): Either<ApiResponse, Visit> = call {
        networkDataSource.createVisit(locationId, visit).toDomainModel()
    }

    override suspend fun updateVisit(
        visitId: String,
        locationId: String,
        visit: VisitFormData
    ): Either<ApiResponse, Visit> = call {
        networkDataSource.updateVisit(visitId, locationId, visit).toDomainModel()
    }

    override suspend fun deleteVisit(visitId: String): Either<ApiResponse, Unit> = call {
        networkDataSource.deleteVisit(visitId)
    }

    private inline fun <T> call(block: () -> T): Either<ApiResponse, T> = try {
        Either.Right(block())
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
