package com.desarrollodroide.adventurelog.feature.locations.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.AddEditAdventureViewModel
import com.desarrollodroide.adventurelog.feature.ui.di.imageLoaderModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val locationsModule = module {
    includes(domainModule, imageLoaderModule)
    
    viewModel { 
        LocationsViewModel(
            getLocationsPagingUseCase = get(),
            getCategoriesUseCase = get(),
            getAllCollectionsUseCase = get(),
            observeCollectionsUseCase = get(),
            deleteLocationUseCase = get(),
            createCategoryUseCase = get(),
            updateCategoryUseCase = get(),
            deleteCategoryUseCase = get(),
            updateLocationCollectionsUseCase = get(),
            duplicateLocationUseCase = get(),
            getShareImageUseCase = get(),
            platformFiles = get()
        ) 
    }
    
    viewModel { params -> 
        AddEditAdventureViewModel(
            createLocationUseCase = get(),
            updateLocationUseCase = get(),
            getLocationUseCase = get(),
            getCategoriesUseCase = get(),
            generateDescriptionUseCase = get(),
            searchLocationsUseCase = get(),
            reverseGeocodeUseCase = get(),
            searchWikipediaImageUseCase = get(),
            createCategoryUseCase = get(),
            uploadImageUseCase = get(),
            syncLocationVisitsUseCase = get(),
            syncLocationTrailsUseCase = get(),
            imageBytesProvider = get(),
            adventureId = params.getOrNull(),
            existingLocation = params.getOrNull()
        )
    }
}
