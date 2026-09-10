package com.desarrollodroide.adventurelog.feature.detail

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Every LocationsRepository call, refusing unless a test says otherwise. */
abstract class LocationsRepositoryStub : LocationsRepository {

    override var selectedLocation: Location? = null

    override fun getLocationsPagingData(): Flow<PagingData<Location>> = flowOf(PagingData.empty())

    override fun getLocationsPagingDataFiltered(
        categoryNames: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): Flow<PagingData<Location>> = flowOf(PagingData.empty())

    override suspend fun getLocations(page: Int, pageSize: Int) = unused()
    override suspend fun getAllLocations(): Either<ApiResponse, List<Location>> = unused()
    override suspend fun getLocation(objectId: String): Either<ApiResponse, Location> = unused()

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
        activityTypes: List<String>
    ): Either<ApiResponse, Location> = unused()

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
        collections: List<String>,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?
    ): Either<ApiResponse, Location> = unused()

    private fun unused(): Nothing =
        throw AssertionError("This test reached a repository call it does not override")
}
