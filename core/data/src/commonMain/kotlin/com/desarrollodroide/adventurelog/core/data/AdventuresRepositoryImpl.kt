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
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.io.IOException

class AdventuresRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : AdventuresRepository {

    private val _adventuresFlow = MutableStateFlow<List<Adventure>>(emptyList())
    override val adventuresFlow: StateFlow<List<Adventure>> = _adventuresFlow.asStateFlow()
    
    // Version counter to force paging invalidation
    private val _version = MutableStateFlow(0)
    
    override fun getAdventuresPagingData(): Flow<PagingData<Adventure>> {
        return _version.flatMapLatest { _ ->
            Pager(
                config = PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false,
                    initialLoadSize = 30,
                    prefetchDistance = 10
                ),
                pagingSourceFactory = { AdventuresPagingSource(networkDataSource, pageSize = 30) }
            ).flow
        }
    }
    
    override fun getAdventuresPagingDataFiltered(
        categoryNames: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): Flow<PagingData<Adventure>> {
        return _version.flatMapLatest { _ ->
            Pager<Int, Adventure>(
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
    
    override suspend fun getAllAdventures(): Either<ApiResponse, List<Adventure>> {
        return try {
            // Load all adventures (up to 1000) for the map
            val adventures = networkDataSource.getAdventures(page = 1, pageSize = 1000).map { it.toDomainModel() }
            
            // Also update the main flow if needed
            _adventuresFlow.value = adventures
            
            Either.Right(adventures)
        } catch (e: HttpException) {
            println("HTTP Error during getAllAdventuresForMap: ${e.code}")
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during getAllAdventuresForMap: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during getAllAdventuresForMap: ${e.message}")
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
    ): Either<ApiResponse, Adventure> {
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
            
            // Increment version to invalidate paging
            _version.value++
            
            Either.Right(adventure)
        } catch (e: HttpException) {
            println("HTTP Error during createAdventure: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during createAdventure: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during createAdventure: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun refreshAdventures(): Either<ApiResponse, List<Adventure>> {
        return try {
            // Load a large page to get all adventures for the map
            // This is called by MapViewModel which needs all adventures
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

    override suspend fun generateDescription(
        name: String
    ): Either<ApiResponse, String> {
        return try {
            val description = networkDataSource.generateDescription(name)
            if (description.isBlank()) {
                Either.Left(ApiResponse.HttpError)
            } else {
                Either.Right(description)
            }
        } catch (e: HttpException) {
            println("HTTP Error during generateDescription: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during generateDescription: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Error during generateDescription: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun deleteAdventure(adventureId: String): Either<ApiResponse, Unit> {
        return try {
            networkDataSource.deleteAdventure(adventureId)
            
            // Remove the deleted adventure from the flow
            _adventuresFlow.value = _adventuresFlow.value.filter { it.id != adventureId }
            
            // Increment version to invalidate paging
            _version.value++
            
            Either.Right(Unit)
        } catch (e: HttpException) {
            println("HTTP Error during deleteAdventure: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during deleteAdventure: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during deleteAdventure: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun updateAdventure(
        adventureId: String,
        name: String,
        description: String,
        category: Category?,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        tags: List<String>
    ): Either<ApiResponse, Adventure> {
        return try {
            val adventure = networkDataSource.updateAdventure(
                adventureId = adventureId,
                name = name,
                description = description,
                category = category,
                rating = rating,
                link = link,
                location = location,
                latitude = latitude,
                longitude = longitude,
                isPublic = isPublic,
                tags = tags
            ).toDomainModel()
            
            // Update the adventure in the flow
            _adventuresFlow.value = _adventuresFlow.value.map { 
                if (it.id == adventureId) adventure else it 
            }
            
            // Increment version to invalidate paging
            _version.value++
            
            Either.Right(adventure)
        } catch (e: HttpException) {
            println("HTTP Error during updateAdventure: ${e.code}")
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            println("IO Error during updateAdventure: ${e.message}")
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            println("Unexpected error during updateAdventure: ${e.message}")
            Either.Left(ApiResponse.HttpError)
        }
    }
}