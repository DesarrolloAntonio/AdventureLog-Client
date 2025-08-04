package com.desarrollodroide.adventurelog.feature.world.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import com.desarrollodroide.adventurelog.feature.world.viewmodel.WorldViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val worldModule = module {
    includes(dataModule)
    
    viewModel {
        WorldViewModel(
            countriesRepository = get()
        )
    }
}