package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.GeocodeRepository
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult

class SearchLocationsUseCase(
    private val geocodeRepository: GeocodeRepository
) {
    suspend operator fun invoke(query: String): Either<String, List<GeocodeSearchResult>> {
        return geocodeRepository.searchLocations(query)
    }
}
