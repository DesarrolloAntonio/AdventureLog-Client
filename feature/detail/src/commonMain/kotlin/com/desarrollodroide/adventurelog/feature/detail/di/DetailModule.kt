package com.desarrollodroide.adventurelog.feature.detail.di

import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.detail.domain.FileHandoff
import com.desarrollodroide.adventurelog.feature.detail.domain.RealFileHandoff

/**
 * Dependencies for the detail feature
 */
val detailModule = module {
    single<FileHandoff> { RealFileHandoff(downloader = get(), files = get()) }

    viewModel { 
        AdventureDetailViewModel(
            getLocationUseCase = get(),
            fileHandoff = get(),
            getShareImageUseCase = get(),
            observeCollectionsUseCase = get()
        )
    }
}
