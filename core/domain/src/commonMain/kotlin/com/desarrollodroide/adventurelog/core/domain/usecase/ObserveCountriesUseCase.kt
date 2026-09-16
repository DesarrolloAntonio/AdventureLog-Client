package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.model.Country
import kotlinx.coroutines.flow.StateFlow

/** The countries as cached, following every change to them - a region ticked elsewhere included. */
class ObserveCountriesUseCase(
    private val countriesRepository: CountriesRepository
) {
    operator fun invoke(): StateFlow<List<Country>> = countriesRepository.countriesFlow
}
