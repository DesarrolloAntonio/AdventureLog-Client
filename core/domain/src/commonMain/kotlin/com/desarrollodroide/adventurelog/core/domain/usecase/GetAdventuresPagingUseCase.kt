package com.desarrollodroide.adventurelog.core.domain.usecase

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.coroutines.flow.Flow

class GetAdventuresPagingUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    operator fun invoke(
        categoryNames: List<String>? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        isVisited: Boolean? = null,
        searchQuery: String? = null,
        includeCollections: Boolean = false
    ): Flow<PagingData<Adventure>> {
        // Check if we actually have any filters applied
        val hasFilters = !categoryNames.isNullOrEmpty() || 
                        (sortBy != null && sortBy != "updated_at") || 
                        (sortOrder != null && sortOrder != "desc") || 
                        isVisited != null || 
                        !searchQuery.isNullOrBlank() ||
                        includeCollections
        
        return if (hasFilters) {
            // Use the filtered endpoint only when filters are actually applied
            adventuresRepository.getAdventuresPagingDataFiltered(
                categoryNames = categoryNames,
                sortBy = sortBy,
                sortOrder = sortOrder,
                isVisited = isVisited,
                searchQuery = searchQuery,
                includeCollections = includeCollections
            )
        } else {
            // Use the regular endpoint when no filters
            adventuresRepository.getAdventuresPagingData()
        }
    }
}
