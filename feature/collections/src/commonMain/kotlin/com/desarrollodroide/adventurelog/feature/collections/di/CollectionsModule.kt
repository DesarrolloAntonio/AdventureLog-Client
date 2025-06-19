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
            getCollectionsUseCase = get()
        ) 
    }
    
    viewModel { 
        CollectionDetailViewModel(
            getCollectionDetailUseCase = get()
        ) 
    }
    
    viewModel { params -> 
        AddEditCollectionViewModel(
            createCollectionUseCase = get(),
            updateCollectionUseCase = get(),
            collectionId = params.getOrNull()
        )
    }
}
