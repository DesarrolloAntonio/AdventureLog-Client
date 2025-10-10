package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AdventuresRepository {

    val adventuresFlow: StateFlow<List<Location>>
    
    fun getAdventuresPagingData(): Flow<PagingData<Location>>
    
    fun getAdventuresPagingDataFiltered(
        categoryNames: List<String>? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        isVisited: Boolean? = null,
        searchQuery: String? = null,
        includeCollections: Boolean = false
    ): Flow<PagingData<Location>>

    suspend fun getAdventures(
        page: Int, pageSize: Int
    ): Either<ApiResponse, List<Location>>
    
    suspend fun getAllAdventures(): Either<ApiResponse, List<Location>>

    suspend fun getAdventure(
        objectId: String
    ): Either<ApiResponse, Location>

    suspend fun createAdventure(
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
        activityTypes: List<String> = emptyList()
    ): Either<ApiResponse, Location>

    suspend fun refreshAdventures(): Either<ApiResponse, List<Location>>

    suspend fun generateDescription(
        name: String
    ): Either<ApiResponse, String>

    suspend fun deleteAdventure(
        adventureId: String
    ): Either<ApiResponse, Unit>
    
    suspend fun updateAdventure(
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
        visits: List<VisitFormData> = emptyList()
    ): Either<ApiResponse, Location>
}
