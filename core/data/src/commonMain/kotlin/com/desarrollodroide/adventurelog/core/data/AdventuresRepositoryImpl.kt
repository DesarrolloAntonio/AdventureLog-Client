package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.paging.AdventuresPagingSource
import com.desarrollodroide.adventurelog.core.data.paging.AdventuresPagingSourceFiltered
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.io.IOException

class AdventuresRepositoryImpl(
    private val networkDataSource: AdventureLogNetworkDataSource
) : AdventuresRepository {

    private val _adventuresFlow = MutableStateFlow<List<Adventure>>(emptyList())
    override val adventuresFlow: StateFlow<List<Adventure>> = _adventuresFlow.asStateFlow()
    
    override fun getAdventuresPagingData(): Flow<PagingData<Adventure>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                initialLoadSize = 30,    // Primera carga igual al pageSize
                prefetchDistance = 10     // Cargar siguiente página cuando falten 10 items
            ),
            pagingSourceFactory = { AdventuresPagingSource(networkDataSource, pageSize = 30) }
        ).flow
    }
    
    override fun getAdventuresPagingDataFiltered(
        categoryNames: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): Flow<PagingData<Adventure>> {
        return Pager<Int, Adventure>(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = false,
                initialLoadSize = 30,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { 
                AdventuresPagingSourceFiltered(
                    networkDataSource = networkDataSource,
                    pageSize = 30,
                    categoryNames = categoryNames,
                    sortBy = sortBy,
                    sortOrder = sortOrder,
                    isVisited = isVisited,
                    searchQuery = searchQuery,
                    includeCollections = includeCollections
                ) 
            }
        ).flow
    }

    override suspend fun getAdventures(page: Int, pageSize: Int): Either<ApiResponse, List<Adventure>> {
        return try {
            val adventures = networkDataSource.getAdventures(page, pageSize).map { it.toDomainModel() }
            
            // For page 1, update the flow
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
        visits: List<VisitFormData>,
        activityTypes: List<String>
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
                visits = visits,
                activityTypes = activityTypes
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
            val adventures = networkDataSource.getAdventures(1, 10).map { it.toDomainModel() }
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

    override suspend fun generateDescription(
        name: String
    ): Either<String, String> {
        return try {
            val description = networkDataSource.generateDescription(name)
            if (description.isBlank()) {
                Either.Left("No description found for $name")
            } else {
                Either.Right(description)
            }
        } catch (e: HttpException) {
            println("HTTP Error during generateDescription: ${e.code}")
            Either.Left("Failed to generate description: HTTP ${e.code}")
        } catch (e: IOException) {
            println("IO Error during generateDescription: ${e.message}")
            Either.Left("Network error: ${e.message}")
        } catch (e: Exception) {
            println("Error during generateDescription: ${e.message}")
            // Handle the "No description found" exception from the network layer
            when (e.message) {
                "No description found" -> Either.Left("No Wikipedia description available for \"$name\"")
                else -> Either.Left("Failed to generate description: ${e.message}")
            }
        }
    }

    override suspend fun deleteAdventure(adventureId: String): Either<String, Unit> {
        return try {
            networkDataSource.deleteAdventure(adventureId)
            
            // Remove the deleted adventure from the flow
            _adventuresFlow.value = _adventuresFlow.value.filter { it.id != adventureId }
            
            Either.Right(Unit)
        } catch (e: HttpException) {
            println("HTTP Error during deleteAdventure: ${e.code}")
            Either.Left("Failed to delete adventure: HTTP ${e.code}")
        } catch (e: IOException) {
            println("IO Error during deleteAdventure: ${e.message}")
            Either.Left("Network error: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error during deleteAdventure: ${e.message}")
            Either.Left("Unexpected error: ${e.message}")
        }
    }
}