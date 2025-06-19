package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Collection

interface CollectionsRepository {

    suspend fun getCollections(page: Int, pageSize: Int): Either<ApiResponse, List<Collection>>
    suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection>
    suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): Either<String, Collection>

}
