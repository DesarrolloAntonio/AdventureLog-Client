package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.toDomainModel
import kotlinx.io.IOException

class AdventuresRepositoryImpl(
    private val networkDataSource: AdventureLogNetworkDataSource
) : AdventuresRepository {

    override suspend fun getAdventures(page: Int, pageSize: Int): Either<ApiResponse, List<Adventure>> {
        return try {
            val adventures = networkDataSource.getAdventures(page, pageSize).map { it.toDomainModel() }
            Either.Right(adventures)
        } catch (e: HttpException) {
            println("HTTP Error during getAdventures: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getAdventures: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getAdventures: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun getAdventure(objectId: String): Either<ApiResponse, Adventure> {
        return try {
            val adventure = networkDataSource.getAdventureDetail(objectId).toDomainModel()
            Either.Right(adventure)
        } catch (e: HttpException) {
            println("HTTP Error during getAdventure: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getAdventure: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getAdventure: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun createAdventure(
        name: String,
        description: String,
        categoryId: String,
        rating: Double,
        link: String,
        location: String,
        latitude: Double?,
        longitude: Double?,
        isPublic: Boolean,
        visitDates: Visit?
    ): Either<String, Adventure> {
        return try {
            val adventure = networkDataSource.createAdventure(
                name = name,
                description = description,
                categoryId = categoryId,
                rating = rating,
                link = link,
                location = location,
                latitude = latitude,
                longitude = longitude,
                isPublic = isPublic,
                visitDates = visitDates
            ).toDomainModel()
            Either.Right(adventure)
        } catch (e: HttpException) {
            println("HTTP Error during createAdventure: ${e.code}")
            Either.Left("Failed to create adventure: HTTP ${e.code}")
        } catch (e: IOException) {
            println("IO Error during createAdventure: ${e.message}")
            Either.Left("Network error: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during createAdventure: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
}
