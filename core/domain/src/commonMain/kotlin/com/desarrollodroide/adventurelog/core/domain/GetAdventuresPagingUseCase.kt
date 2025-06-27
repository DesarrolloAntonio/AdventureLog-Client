package com.desarrollodroide.adventurelog.core.domain

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.coroutines.flow.Flow

class GetAdventuresPagingUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    operator fun invoke(): Flow<PagingData<Adventure>> =
        adventuresRepository.getAdventuresPagingData()
}