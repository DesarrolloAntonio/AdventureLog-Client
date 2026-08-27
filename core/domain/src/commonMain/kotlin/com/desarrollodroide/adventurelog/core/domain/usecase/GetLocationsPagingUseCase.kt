package com.desarrollodroide.adventurelog.core.domain.usecase

import app.cash.paging.PagingData
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.model.Location
import kotlinx.coroutines.flow.Flow
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("GetLocationsPagingUseCase")

class GetLocationsPagingUseCase(
    private val locationsRepository: LocationsRepository
) {
    operator fun invoke(
        categoryNames: List<String>? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        isVisited: Boolean? = null,
        searchQuery: String? = null,
        includeCollections: Boolean = false
    ): Flow<PagingData<Location>> {
        val hasFilters = !categoryNames.isNullOrEmpty() || 
                        (sortBy != null && sortBy != "updated_at") || 
                        (sortOrder != null && sortOrder != "desc") || 
                        isVisited != null || 
                        !searchQuery.isNullOrBlank() ||
                        includeCollections
        
        return if (hasFilters) {
            locationsRepository.getLocationsPagingDataFiltered(
                categoryNames = categoryNames,
                sortBy = sortBy,
                sortOrder = sortOrder,
                isVisited = isVisited,
                searchQuery = searchQuery,
                includeCollections = includeCollections
            )
        } else {
            locationsRepository.getLocationsPagingData()
        }
    }
    
    fun selectLocation(location: Location) {
        logger.d { "🟢 [GetLocationsPagingUseCase] Setting selectedLocation: ${location.id} - ${location.name}" }
        locationsRepository.selectedLocation = location
        logger.d { "🟢 [GetLocationsPagingUseCase] selectedLocation is now: ${locationsRepository.selectedLocation?.name}" }
    }
}
