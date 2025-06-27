package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.io.IOException

class AdventuresRepositoryImpl(
    private val networkDataSource: AdventureLogNetworkDataSource
) : AdventuresRepository {

    private val _adventuresFlow = MutableStateFlow<List<Adventure>>(emptyList())
    override val adventuresFlow: StateFlow<List<Adventure>> = _adventuresFlow.asStateFlow()

    override suspend fun getAdventures(page: Int, pageSize: Int): Either<ApiResponse, List<Adventure>> {
        // If we already have adventures cached and it's the first page, return from cache
        if (page == 1 && _adventuresFlow.value.isNotEmpty()) {
            val cachedAdventures = _adventuresFlow.value
            val requestedAdventures = if (pageSize >= cachedAdventures.size) {
                cachedAdventures
            } else {
                cachedAdventures.take(pageSize)
            }
            return Either.Right(requestedAdventures)
        }

        return try {
            val adventures = networkDataSource.getAdventures(page, pageSize).map { it.toDomainModel() }
            
            // Update the flow with the new adventures (only for first page)
            if (page == 1) {
                _adventuresFlow.value = adventures
            }
            
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
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visitDates: Visit?
    ): Either<String, Adventure> {
        return try {
            val adventure = networkDataSource.createAdventure(
                name = name,
                description = description,
                category = category,
                rating = rating,
                link = link,
                location = location,
                latitude = latitude,
                longitude = longitude,
                isPublic = isPublic,
                visitDates = visitDates
            ).toDomainModel()
            
            // Add the new adventure to the beginning of the flow (most recent first)
            _adventuresFlow.value = listOf(adventure) + _adventuresFlow.value
            
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

    override suspend fun refreshAdventures(): Either<ApiResponse, List<Adventure>> {
        return try {
            val adventures = networkDataSource.getAdventures(1, 1000).map { it.toDomainModel() }
            _adventuresFlow.value = adventures
            Either.Right(adventures)
        } catch (e: HttpException) {
            println("HTTP Error during refreshAdventures: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during refreshAdventures: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during refreshAdventures: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
}
