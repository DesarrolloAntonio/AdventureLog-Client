package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult

interface GeocodeRepository {
    suspend fun searchLocations(query: String): Either<String, List<GeocodeSearchResult>>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Either<String, ReverseGeocodeResult>
}
