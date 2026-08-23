package com.desarrollodroide.adventurelog.core.domain.repository

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Location
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
        activityTypes: List<String> = emptyList()
    ): Either<ApiResponse, Location>

    suspend fun refreshLocations(): Either<ApiResponse, List<Location>>

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
        collections: List<String> = emptyList(),
        visits: List<VisitFormData> = emptyList(),
        price: Double? = null,
        priceCurrency: String? = null
    ): Either<ApiResponse, Location>
}
