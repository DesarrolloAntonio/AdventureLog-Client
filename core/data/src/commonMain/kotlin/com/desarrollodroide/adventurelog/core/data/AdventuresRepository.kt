package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AdventuresRepository {

    val adventuresFlow: StateFlow<List<Adventure>>
    
    fun getAdventuresPagingData(): Flow<PagingData<Adventure>>
    
    fun getAdventuresPagingDataFiltered(
        categoryNames: List<String>? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        isVisited: Boolean? = null,
        searchQuery: String? = null,
        includeCollections: Boolean = false
    ): Flow<PagingData<Adventure>>

    suspend fun getAdventures(
        page: Int, pageSize: Int
    ): Either<ApiResponse, List<Adventure>>

    suspend fun getAdventure(
        objectId: String
    ): Either<ApiResponse, Adventure>

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
    ): Either<String, Adventure>

    suspend fun refreshAdventures(): Either<ApiResponse, List<Adventure>>

    suspend fun generateDescription(
        name: String
    ): Either<String, String>
}
