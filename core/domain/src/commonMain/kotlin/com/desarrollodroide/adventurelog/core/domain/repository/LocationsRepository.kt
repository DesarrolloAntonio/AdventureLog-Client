package com.desarrollodroide.adventurelog.core.domain.repository

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

interface LocationsRepository {

    var selectedLocation: Location?
    
    fun getLocationsPagingData(): Flow<PagingData<Location>>
    
    fun getLocationsPagingDataFiltered(
        categoryNames: List<String>? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        isVisited: Boolean? = null,
        searchQuery: String? = null,
        includeCollections: Boolean = false
    ): Flow<PagingData<Location>>

    suspend fun getLocations(
        page: Int, pageSize: Int
    ): Either<ApiResponse, List<Location>>
    
    suspend fun getAllLocations(): Either<ApiResponse, List<Location>>

    suspend fun getLocation(
        objectId: String
    ): Either<ApiResponse, Location>

    /** The place as the server has it now - never a copy kept from a list. */
    suspend fun fetchLocation(objectId: String): Either<ApiResponse, Location>

    /** Changes which collections the place is in and nothing else. */
    suspend fun updateLocationCollections(locationId: String, collections: List<String>): Either<ApiResponse, Location>

    suspend fun createLocation(
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
        activityTypes: List<String> = emptyList(),
        /** Collections the new place joins straight away, so no second call is needed. */
        collectionIds: List<String> = emptyList()
    ): Either<ApiResponse, Location>

    /**
     * Places near a point that are not in the account yet. Either the coordinates or [place] must
     * be given; the server geocodes the latter.
     */
    suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): Either<ApiResponse, List<Recommendation>>

    suspend fun refreshLocations(): Either<ApiResponse, List<Location>>

    /** Asks the server for a copy of a location. */
    suspend fun duplicateLocation(locationId: String): Either<ApiResponse, Location>

    /** The PNG the server renders for sharing a location. */
    suspend fun getShareImage(locationId: String, aspect: String): Either<ApiResponse, ByteArray>

    suspend fun generateDescription(
        name: String
    ): Either<ApiResponse, String>

    suspend fun deleteLocation(
        adventureId: String
    ): Either<ApiResponse, Unit>
    
    suspend fun updateLocation(
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
        collections: List<String>? = null,
        visits: List<VisitFormData> = emptyList(),
        price: Double? = null,
        priceCurrency: String? = null
    ): Either<ApiResponse, Location>
}
