package com.desarrollodroide.adventurelog.feature.detail.di

import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Dependencies for the detail feature
 */
val detailModule = module {
    // Register the AdventureDetailViewModel with ObserveCollectionsUseCase
    viewModel { AdventureDetailViewModel(get()) }
}
