package com.desarrollodroide.adventurelog.feature.detail.di

import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Dependencies for the detail feature
 */
val detailModule = module {
    viewModel { 
        AdventureDetailViewModel(
            getLocationUseCase = get(),
            fileDownloader = get(),
            platformFiles = get(),
            getShareImageUseCase = get(),
            observeCollectionsUseCase = get()
        )
    }
}
