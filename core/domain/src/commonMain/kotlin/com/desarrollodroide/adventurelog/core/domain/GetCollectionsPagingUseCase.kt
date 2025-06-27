package com.desarrollodroide.adventurelog.core.domain

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.flow.Flow

class GetCollectionsPagingUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    operator fun invoke(): Flow<PagingData<Collection>> =
        collectionsRepository.getCollectionsPagingData()
}
