package com.desarrollodroide.adventurelog.core.testing

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a LocationsRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class LocationsRepositoryStub : LocationsRepository {
    override var selectedLocation: Location? = null
    override fun getLocationsPagingData(): Flow<PagingData<Location>> = unused()
    override fun getLocationsPagingDataFiltered(
        categoryNames: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): Flow<PagingData<Location>> = unused()
    override suspend fun getLocations(page: Int, pageSize: Int): Either<ApiResponse, List<Location>> = unused()
    override suspend fun getAllLocations(): Either<ApiResponse, List<Location>> = unused()
    override suspend fun getLocation(objectId: String): Either<ApiResponse, Location> = unused()
    override suspend fun fetchLocation(objectId: String): Either<ApiResponse, Location> = unused()
    override suspend fun updateLocationCollections(locationId: String, collections: List<String>): Either<ApiResponse, Location> = unused()
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
    ): Either<ApiResponse, Location> = unused()
    override suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): Either<ApiResponse, List<Recommendation>> = unused()
    override suspend fun refreshLocations(): Either<ApiResponse, List<Location>> = unused()
    override suspend fun duplicateLocation(locationId: String): Either<ApiResponse, Location> = unused()
    override suspend fun getShareImage(locationId: String, aspect: String): Either<ApiResponse, ByteArray> = unused()
    override suspend fun generateDescription(name: String): Either<ApiResponse, String> = unused()
    override suspend fun deleteLocation(adventureId: String): Either<ApiResponse, Unit> = unused()
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
        collections: List<String>?,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?
    ): Either<ApiResponse, Location> = unused()
}
