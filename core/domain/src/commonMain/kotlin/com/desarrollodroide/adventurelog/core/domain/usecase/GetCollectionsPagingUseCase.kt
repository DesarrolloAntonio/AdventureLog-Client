package com.desarrollodroide.adventurelog.core.domain.usecase

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.Flow

class GetCollectionsPagingUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    operator fun invoke(
        sortField: String? = null,
        sortDirection: String? = null
    ): Flow<PagingData<UltraSlimCollection>> =
        collectionsRepository.getCollectionsPagingData(sortField, sortDirection)
}
