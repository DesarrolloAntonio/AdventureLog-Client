package com.desarrollodroide.adventurelog.feature.collections.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionDetailViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditTransportationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val collectionsModule = module {
    includes(domainModule)

    viewModel {
        CollectionsViewModel(
            getCollectionsPagingUseCase = get(),
            getAllCollectionsUseCase = get(),
            deleteCollectionUseCase = get(),
            observeCollectionsUseCase = get()
        )
    }

    viewModel {
        CollectionDetailViewModel(
            getCollectionDetailUseCase = get(),
            deleteTransportationUseCase = get(),
            observeCollectionsUseCase = get(),
            deleteLocationUseCase = get(),
            updateLocationCollectionsUseCase = get(),
            getAllCollectionsUseCase = get(),
        )
    }

    viewModel { params ->
        AddEditCollectionViewModel(
            collectionId = params.getOrNull(),
            createCollectionUseCase = get(),
            getCollectionDetailUseCase = get(),
            updateCollectionUseCase = get()
        )
    }

    viewModel { params -> 
        AddEditTransportationViewModel(
            createTransportationUseCase = get(),
            updateTransportationUseCase = get(),
            getTransportationUseCase = get(),
            generateDescriptionUseCase = get(),
            searchLocationsUseCase = get(),
            searchWikipediaImageUseCase = get(),
            transportationId = params.get(0),
            existingTransportation = params.getOrNull(),
            collectionId = params.get(2)
        )
    }
}
