package com.desarrollodroide.adventurelog.feature.adventures.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AddEditAdventureViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val locationsModule = module {
    includes(domainModule)
    
    viewModel { 
        AdventuresViewModel(
            getAdventuresPagingUseCase = get(),
            getCategoriesUseCase = get(),
            getAllCollectionsUseCase = get(),
            observeCollectionsUseCase = get(),
            deleteAdventureUseCase = get(),
            createCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            deleteCategoryUseCase = get(),
            updateAdventureCollectionsUseCase = get()
        ) 
    }
    
    viewModel { params -> 
        AddEditAdventureViewModel(
            createAdventureUseCase = get(),
            updateAdventureUseCase = get(),
            getAdventureUseCase = get(),
            getCategoriesUseCase = get(),
            generateDescriptionUseCase = get(),
            searchLocationsUseCase = get(),
            reverseGeocodeUseCase = get(),
            searchWikipediaImageUseCase = get(),
            adventureId = params.getOrNull(),
            existingLocation = params.getOrNull()
        )
    }
}
