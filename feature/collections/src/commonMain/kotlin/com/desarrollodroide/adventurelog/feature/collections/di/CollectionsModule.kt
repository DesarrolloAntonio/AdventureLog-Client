package com.desarrollodroide.adventurelog.feature.collections.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionDetailViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.RecommendationsViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditTransportationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditChecklistViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditLodgingViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditNoteViewModel

val collectionsModule = module {
    includes(domainModule)

    viewModel {
        CollectionsViewModel(
            getCollectionsPagingUseCase = get(),
            getAllCollectionsUseCase = get(),
            deleteCollectionUseCase = get(),
            observeCollectionsUseCase = get(),
            duplicateCollectionUseCase = get(),
            archiveCollectionUseCase = get(),
            exportCollectionUseCase = get(),
            platformFiles = get(),
            getArchivedCollectionsUseCase = get(),
            getSharedCollectionsUseCase = get(),
            getCollectionInvitesUseCase = get(),
            respondToCollectionInviteUseCase = get(),
            sharingRepository = get()
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
            deleteNoteUseCase = get(),
            deleteChecklistUseCase = get(),
            deleteLodgingUseCase = get(),
            autoGenerateItineraryUseCase = get(),
            addItineraryEntryUseCase = get(),
            deleteItineraryEntryUseCase = get(),
            duplicateLocationUseCase = get(),
            getShareImageUseCase = get(),
            removeLocationFromCollectionUseCase = get(),
            platformFiles = get(),
        )
    }

    viewModel {
        RecommendationsViewModel(
            getRecommendationsUseCase = get(),
            createLocationUseCase = get(),
            getCategoriesUseCase = get()
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
            getCollectionDetailUseCase = get(),
            transportationId = params.get(0),
            existingTransportation = params.getOrNull(),
            collectionId = params.get(2)
        )
    }

    viewModel {
        AddEditNoteViewModel(saveNoteUseCase = get(), getCollectionItemUseCase = get())
    }

    viewModel {
        AddEditChecklistViewModel(saveChecklistUseCase = get(), getCollectionItemUseCase = get())
    }

    viewModel {
        AddEditLodgingViewModel(saveLodgingUseCase = get(), getCollectionItemUseCase = get())
    }
}
