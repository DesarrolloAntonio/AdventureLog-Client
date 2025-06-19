package com.desarrollodroide.adventurelog.feature.adventures.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AddEditAdventureViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adventureModule = module {
    includes(domainModule)
    
    viewModel { 
        AdventuresViewModel(
            getAdventuresUseCase = get()
        ) 
    }
    
    viewModel { params -> 
        AddEditAdventureViewModel(
            createAdventureUseCase = get(),
            updateAdventureUseCase = get(),
            adventureId = params.getOrNull()
        )
    }
}
