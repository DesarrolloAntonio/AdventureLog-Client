package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.data.CountriesRepository

class GetVisitedCitiesUseCase(
    private val countriesRepository: CountriesRepository
) {
    suspend operator fun invoke() {
        countriesRepository.getVisitedCities()
    }
}
