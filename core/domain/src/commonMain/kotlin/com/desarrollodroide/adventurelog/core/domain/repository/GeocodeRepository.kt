package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult

interface GeocodeRepository {
    suspend fun searchLocations(query: String): Either<ApiResponse, List<GeocodeSearchResult>>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Either<ApiResponse, ReverseGeocodeResult>
}
