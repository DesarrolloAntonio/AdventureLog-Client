package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TrailsRepository
import com.desarrollodroide.adventurelog.core.model.Trail
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class TrailsRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : TrailsRepository {

    override suspend fun createTrail(
        locationId: String,
        trail: TrailFormData
    ): Either<ApiResponse, Trail> = call {
        networkDataSource.createTrail(locationId, trail).toDomainModel()
    }

    override suspend fun updateTrail(
        trailId: String,
        locationId: String,
        trail: TrailFormData
    ): Either<ApiResponse, Trail> = call {
        networkDataSource.updateTrail(trailId, locationId, trail).toDomainModel()
    }

    override suspend fun deleteTrail(trailId: String): Either<ApiResponse, Unit> = call {
        networkDataSource.deleteTrail(trailId)
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
