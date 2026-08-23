package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.DashboardRepository
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class DashboardRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : DashboardRepository {

    override suspend fun getDashboard(): Either<ApiResponse, Dashboard> {
        return try {
            Either.Right(networkDataSource.getDashboard().toDomainModel())
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
