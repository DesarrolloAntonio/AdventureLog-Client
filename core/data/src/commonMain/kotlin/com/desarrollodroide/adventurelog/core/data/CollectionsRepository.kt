package com.desarrollodroide.adventurelog.core.data

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CollectionsRepository {

    val collectionsFlow: StateFlow<List<Collection>>

    fun getCollectionsPagingData(
        sortField: String? = null,
        sortDirection: String? = null
    ): Flow<PagingData<Collection>>

    suspend fun getCollections(page: Int, pageSize: Int): Either<ApiResponse, List<Collection>>
    suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection>
    suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): Either<ApiResponse, Collection>

    suspend fun refreshCollections(): Either<ApiResponse, List<Collection>>

    suspend fun deleteCollection(collectionId: String): Either<ApiResponse, Unit>

    suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): Either<ApiResponse, Collection>

}
