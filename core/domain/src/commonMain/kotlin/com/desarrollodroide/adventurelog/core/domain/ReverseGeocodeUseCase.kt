package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.GeocodeRepository
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult

class ReverseGeocodeUseCase(
    private val geocodeRepository: GeocodeRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Either<String, ReverseGeocodeResult> {
        return geocodeRepository.reverseGeocode(latitude, longitude)
    }
}
