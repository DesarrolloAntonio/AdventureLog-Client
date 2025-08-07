package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.io.IOException

class CategoriesRepositoryImpl(
    private val networkDataSource: AdventureLogNetworkDataSource
) : CategoriesRepository {

    override suspend fun getCategories(): Either<ApiResponse, List<Category>> {
        return try {
            val categories = networkDataSource.getCategories().map { it.toDomainModel() }
            Either.Right(categories)
        } catch (e: HttpException) {
            println("HTTP Error during getCategories: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getCategories: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getCategories: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
}
