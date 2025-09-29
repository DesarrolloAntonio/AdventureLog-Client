package com.desarrollodroide.adventurelog.feature.collections.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionDetailViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val collectionsModule = module {
    includes(domainModule)

    viewModel {
        CollectionsViewModel(
            getCollectionsPagingUseCase = get(),
            getAllCollectionsUseCase = get(),
            deleteCollectionUseCase = get()
        )
    }

    viewModel {
        CollectionDetailViewModel(
            getCollectionDetailUseCase = get(),
            deleteAdventureUseCase = get(),
            deleteTransportationUseCase = get(),
            updateAdventureCollectionsUseCase = get(),
            observeCollectionsUseCase = get(),
            getAllCollectionsUseCase = get()
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
}
