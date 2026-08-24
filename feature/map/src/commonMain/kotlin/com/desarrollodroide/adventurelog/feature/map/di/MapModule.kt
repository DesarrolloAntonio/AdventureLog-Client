package com.desarrollodroide.adventurelog.feature.map.di

import com.desarrollodroide.adventurelog.feature.map.viewmodel.MapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
    viewModel { MapViewModel(
        getAllLocationsUseCase = get(),
        observeUserStatsUseCase = get(),
        getVisitedRegionsUseCase = get(),
        getVisitedCitiesUseCase = get(),
        userRepository = get())
    }
}
