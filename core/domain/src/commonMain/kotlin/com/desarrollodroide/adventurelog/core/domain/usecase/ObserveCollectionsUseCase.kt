package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.coroutines.flow.StateFlow

class ObserveCollectionsUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    operator fun invoke(): StateFlow<List<UltraSlimCollection>> = collectionsRepository.collectionsFlow
}
