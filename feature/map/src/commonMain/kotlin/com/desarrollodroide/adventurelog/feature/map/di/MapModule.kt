package com.desarrollodroide.adventurelog.feature.map.di

import com.desarrollodroide.adventurelog.feature.map.viewmodel.MapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mapModule = module {
    viewModel { MapViewModel(
        getAllLocationsUseCase = get(),
        getVisitedRegionsUseCase = get(),
        getVisitedCitiesUseCase = get(),
        // A ViewModel definition hands over the SavedStateHandle the filters are kept in.
        savedStateHandle = get())
    }
}
