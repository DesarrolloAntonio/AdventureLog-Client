package com.desarrollodroide.adventurelog.feature.settings.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel

val settingsModule = module {
    includes(dataModule)
    factory {
        SettingsViewModel(
            settingsRepository = get(),
            imageLoader = get()
        )
    }
    viewModelOf(::SettingsViewModel)

}