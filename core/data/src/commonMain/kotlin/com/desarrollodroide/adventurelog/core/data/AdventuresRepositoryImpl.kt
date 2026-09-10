package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.paging.AdventuresPagingSource
import com.desarrollodroide.adventurelog.core.data.paging.AdventuresPagingSourceFiltered
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.io.IOException
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("AdventuresRepositoryImpl")

class AdventuresRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : LocationsRepository {

    override var selectedLocation: Location? = null

    // Version counter to force paging invalidation
    private val _version = MutableStateFlow(0)
    
    override fun getLocationsPagingData(): Flow<PagingData<Location>> {
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
    
    override fun getLocationsPagingDataFiltered(
        categoryNames: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): Flow<PagingData<Location>> {
        return _version.flatMapLatest { _ ->
            Pager<Int, Location>(
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

    override suspend fun getLocations(page: Int, pageSize: Int): Either<ApiResponse, List<Location>> {
        return try {
            val adventures = networkDataSource.getAdventures(page, pageSize).map { it.toDomainModel() }
            Either.Right(adventures)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getAdventures: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getAdventures: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getAdventures: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun getAllLocations(): Either<ApiResponse, List<Location>> {
        return try {
            val adventures = networkDataSource.getAdventures(page = 1, pageSize = 1000).map { it.toDomainModel() }
            Either.Right(adventures)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getAllAdventuresForMap: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getAllAdventuresForMap: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getAllAdventuresForMap: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun getLocation(objectId: String): Either<ApiResponse, Location> {
        val cached = selectedLocation
        logger.d { "🔍 [Repository] getLocation called for: $objectId" }
        logger.d { "🔍 [Repository] selectedLocation is: ${cached?.id} - ${cached?.name}" }
        
        if (cached != null && cached.id == objectId) {
            logger.d { "✨ Using selectedLocation for: $objectId" }
            return Either.Right(cached)
        }
        
        logger.e { "⚠️ selectedLocation not available, fetching from network: $objectId" }
        return try {
            val location = networkDataSource.getAdventureDetail(objectId).toDomainModel()
            Either.Right(location)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getLocation: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getLocation: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getLocation: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun createLocation(
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
        price: Double?,
        priceCurrency: String?,
        activityTypes: List<String>,
        collectionIds: List<String>
    ): Either<ApiResponse, Location> {
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
            price = price,
            priceCurrency = priceCurrency,
                activityTypes = activityTypes,
                collectionIds = collectionIds
            ).toDomainModel()
            
            // Increment version to invalidate paging
            _version.value++
            
            Either.Right(adventure)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during createAdventure: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during createAdventure: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during createAdventure: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): Either<ApiResponse, List<Recommendation>> {
        return try {
            Either.Right(
                networkDataSource.getRecommendations(
                    latitude, longitude, place, category, radiusMetres
                )
            )
        } catch (e: HttpException) {
            logger.e { "HTTP Error during getRecommendations: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during getRecommendations: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during getRecommendations: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun refreshLocations(): Either<ApiResponse, List<Location>> {
        return try {
            val adventures = networkDataSource.getAdventures(1, 1000).map { it.toDomainModel() }
            Either.Right(adventures)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during refreshAdventures: ${e.code}" }
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during refreshAdventures: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during refreshAdventures: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun duplicateLocation(locationId: String): Either<ApiResponse, Location> =
        call {
            val copy = networkDataSource.duplicateLocation(locationId).toDomainModel()
            // A duplicate is a new place in the list, so the paged list has to be told, the same
            // way create and delete tell it. Without this the snackbar named a copy that was not
            // on screen until the screen was left and re-entered.
            _version.value++
            copy
        }

    override suspend fun getShareImage(
        locationId: String,
        aspect: String
    ): Either<ApiResponse, ByteArray> = call {
        networkDataSource.getShareImage(locationId, aspect)
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
            logger.e { "HTTP Error during generateDescription: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during generateDescription: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Error during generateDescription: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun deleteLocation(adventureId: String): Either<ApiResponse, Unit> {
        return try {
            networkDataSource.deleteAdventure(adventureId)

            // Increment version to invalidate paging
            _version.value++

            // Don't leave a deleted place in the cache getLocation() answers from.
            if (selectedLocation?.id == adventureId) {
                selectedLocation = null
            }

            Either.Right(Unit)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during deleteAdventure: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during deleteAdventure: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during deleteAdventure: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
    
    override suspend fun updateLocation(
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
        tags: List<String>,
        collections: List<String>,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?
    ): Either<ApiResponse, Location> {
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
                tags = tags,
                collections = collections,
                visits = visits,
            price = price,
            priceCurrency = priceCurrency
            ).toDomainModel()

            // Increment version to invalidate paging
            _version.value++

            // getLocation() answers from selectedLocation whenever the ids match, so leaving the
            // pre-edit copy there means opening the place you just edited shows what it used to
            // say. Replace it with what the server just returned.
            if (selectedLocation?.id == adventure.id) {
                selectedLocation = adventure
            }

            Either.Right(adventure)
        } catch (e: HttpException) {
            logger.e { "HTTP Error during updateAdventure: ${e.code}" }
            when (e.code) {
                401, 403 -> Either.Left(ApiResponse.InvalidCredentials)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: IOException) {
            logger.e { "IO Error during updateAdventure: ${e.message}" }
            Either.Left(ApiResponse.IOException)
        } catch (e: Exception) {
            logger.e { "Unexpected error during updateAdventure: ${e.message}" }
            Either.Left(ApiResponse.HttpError)
        }
    }
}