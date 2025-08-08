package com.desarrollodroide.adventurelog.feature.world.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.world.viewmodel.WorldViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val worldModule = module {
    includes(domainModule)
    
    viewModel {
        WorldViewModel(
            getCountriesUseCase = get(),
            refreshCountriesUseCase = get(),
            getVisitedRegionsUseCase = get(),
            getVisitedCitiesUseCase = get()
        )
    }
}