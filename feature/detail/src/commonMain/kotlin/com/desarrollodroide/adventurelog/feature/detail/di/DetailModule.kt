package com.desarrollodroide.adventurelog.feature.detail.di

import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import org.koin.dsl.module

/**
 * Dependencies for the detail feature
 */
val detailModule = module {
    // Register the AdventureDetailViewModel
    factory { AdventureDetailViewModel() }
}
