package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.coroutines.flow.StateFlow

class ObserveCollectionsUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    operator fun invoke(): StateFlow<List<Collection>> = collectionsRepository.collectionsFlow
}
